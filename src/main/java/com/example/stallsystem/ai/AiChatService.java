package com.example.stallsystem.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.entity.Product;
import com.example.stallsystem.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 对话服务
 *
 * <p>异步编排说明（一条完整的异步调用链路）：</p>
 * <pre>
 * 请求线程
 *   ├─ aiExecutor: detectIntent()     意图识别（LLM，预算 1.2s，超时降级 chat）
 *   └─ aiExecutor: safeSearch()       知识检索（向量库，预算 1.5s，超时降级空集合）
 *        ↓ 两个互不依赖的远程调用并行执行，而不是串行等待
 *   聚合结果 → aiExecutor: 生成回答（LLM，整链路预算 8s，超时给兜底话术）
 * </pre>
 *
 * <p>为什么要 Semaphore：orTimeout 只是让 Future 提前完成，底层 HTTP 请求还在跑。
 * 如果不限并发，外部接口一慢就会把 aiExecutor 的线程占满，超时越多堆积越多。</p>
 */
@Slf4j
@Service
public class AiChatService {

    /** 同时打到 LLM 的请求上限，和 aiExecutor 的线程数匹配 */
    private static final int LLM_PERMITS = 8;
    /** 单次问答的总时间预算 */
    private static final long TOTAL_BUDGET_SECONDS = 8;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ProductService productService;
    private final ThreadPoolTaskExecutor aiExecutor;
    private final Semaphore llmPermits = new Semaphore(LLM_PERMITS);

    public AiChatService(ChatClient chatClient,
                         VectorStore vectorStore,
                         ProductService productService,
                         @Qualifier("aiExecutor") ThreadPoolTaskExecutor aiExecutor) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.productService = productService;
        this.aiExecutor = aiExecutor;
    }

    public ChatAnswer ask(String question, List<Map<String, String>> history) {
        long start = System.currentTimeMillis();

        // ① 并行扇出：意图识别 + 知识检索互不依赖，同时发起，各带独立超时
        CompletableFuture<String> intentFuture = CompletableFuture
                .supplyAsync(() -> detectIntent(question), aiExecutor)
                .orTimeout(1200, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    log.warn("[chat] 意图识别超时/失败，降级为 chat，traceId={}", MDC.get("traceId"));
                    return "chat";
                });

        CompletableFuture<List<Document>> docsFuture = CompletableFuture
                .supplyAsync(() -> safeSearch(question), aiExecutor)
                .orTimeout(1500, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    log.warn("[chat] 知识检索超时/失败，降级为空参考，traceId={}", MDC.get("traceId"));
                    return List.of();
                });

        String intent = intentFuture.join();
        List<Document> docs = docsFuture.join();
        log.info("[chat] 扇出完成 intent={} 知识={}条 耗时={}ms",
                intent, docs.size(), System.currentTimeMillis() - start);

        // ② 生成阶段：整条链路一个总预算，超时给兜底话术（接口不 500）
        ChatAnswer answer = CompletableFuture
                .supplyAsync(() -> "shop".equals(intent)
                        ? handleShop(question)
                        : handleChat(question, docs, history), aiExecutor)
                .orTimeout(TOTAL_BUDGET_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("[chat] 生成阶段降级，traceId={} 原因={}", MDC.get("traceId"), ex.toString());
                    return new ChatAnswer("小市有点忙，稍后再问我一次吧～", List.of());
                })
                .join();

        log.info("[chat] 全部完成 总耗时={}ms", System.currentTimeMillis() - start);
        return answer;
    }

    /**
     * 所有 LLM 调用的统一入口：并发许可 + 限时获取，拿不到就快速失败走降级。
     */
    private ChatResponse callLlm(Prompt prompt) {
        boolean acquired = false;
        try {
            acquired = llmPermits.tryAcquire(3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("AI 并发已达上限（" + LLM_PERMITS + "）");
            }
            return chatClient.call(prompt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 调用被中断", e);
        } finally {
            if (acquired) {
                llmPermits.release();
            }
        }
    }

    private String detectIntent(String question) {
        try {
            String prompt = """
                    判断用户意图，只返回一个词：
                    - shop: 明确想买东西、点单、问"有什么好吃/好喝的"、想点饮品小吃
                    - chat: 闲聊、讲个故事、心情、打招呼、你好吗、随便聊聊
                    - faq: 问政策、办证、摊位规定、营业时间、投诉

                    用户：%s
                    只返回 shop / chat / faq。
                    """.formatted(question);
            ChatResponse resp = callLlm(new Prompt(new UserMessage(prompt)));
            String r = resp.getResult().getOutput().getContent().trim().toLowerCase();
            if (r.contains("shop")) return "shop";
            if (r.contains("faq")) return "faq";
            return "chat";
        } catch (Exception e) {
            return "chat";
        }
    }

    /**
     * 知识检索：向量库不可用时返回空集合，不拖垮整条链路。
     */
    private List<Document> safeSearch(String question) {
        try {
            List<Document> docs = vectorStore.similaritySearch(
                    SearchRequest.query(question).withTopK(3));
            return docs == null ? List.of() : docs;
        } catch (Exception e) {
            log.warn("[chat] 向量库不可用：{}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 购物意图：从商品库找匹配的
     */
    private ChatAnswer handleShop(String question) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStatus, 1);
        List<Product> products = productService.list(qw);

        String productMenu = products.stream()
                .map(p -> p.getId() + ":" + p.getProductName() + "(¥" + p.getPrice() + ")")
                .collect(Collectors.joining(", "));

        String prompt = """
                你是暖心管家"小市"，用户说：%s
                在售商品：%s
                用1-2句话像朋友推荐美食，简短自然，不要列ID不要列表。
                没合适的就说"今天好像没这个呢~"
                """.formatted(question, productMenu);

        ChatResponse resp = callLlm(new Prompt(new UserMessage(prompt)));
        String answer = resp.getResult().getOutput().getContent();

        // 让 LLM 推荐的商品名做简单匹配，返回前端商品卡片
        List<String> matched = products.stream()
                .filter(p -> answer.contains(p.getProductName()))
                .map(p -> p.getId() + "|" + p.getProductName() + "|" + p.getPrice())
                .collect(Collectors.toList());

        return new ChatAnswer(answer, matched);
    }

    /**
     * 普通对话 / 政策问答：参考知识由上游并行检索好传进来，这里不再自己查向量库。
     */
    private ChatAnswer handleChat(String question, List<Document> relevantDocs, List<Map<String, String>> history) {
        String context = "";
        List<String> snippets = List.of();
        if (relevantDocs != null && !relevantDocs.isEmpty()) {
            context = relevantDocs.stream().map(Document::getContent).collect(Collectors.joining("\n---\n"));
            snippets = relevantDocs.stream().map(Document::getContent).collect(Collectors.toList());
        }

        String historyText = "";
        if (history != null && !history.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map<String, String> h : history) {
                String role = "user".equals(h.get("role")) ? "用户" : "小市";
                sb.append(role).append(": ").append(h.get("content")).append("\n");
            }
            historyText = sb.toString();
        }

        String systemPrompt = """
                你是"小市"，摆摊集市的智能助手，性格活泼，会陪聊会讲故事。
                规则：
                - 回复短！最多3句话
                - 接上用户上一句话继续聊，不要另起话题
                - 用户要讲故事就讲故事，要讲鬼故事就讲鬼故事，不要拒绝不要转正能量
                - 用户情绪不好就安慰
                - 政策类问题基于【参考知识】回答
                - 自然聊天，不要说教

                【之前的对话】
                %s
                【参考知识】
                %s
                """.formatted(historyText, context);

        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(question)));
        ChatResponse response = callLlm(prompt);
        return new ChatAnswer(response.getResult().getOutput().getContent(), snippets);
    }

    public record ChatAnswer(
            String answer,
            List<String> products  // 购物意图时返回 "id|name|price" 列表
    ) {
    }
}
