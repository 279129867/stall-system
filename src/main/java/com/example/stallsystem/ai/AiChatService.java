package com.example.stallsystem.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.entity.Product;
import com.example.stallsystem.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ProductService productService;

    public AiChatService(ChatClient chatClient, VectorStore vectorStore, ProductService productService) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.productService = productService;
    }

    public ChatAnswer ask(String question, List<Map<String, String>> history) {
        String intent = detectIntent(question);
        log.info("用户意图: {}", intent);

        if ("shop".equals(intent)) {
            return handleShop(question);
        }
        return handleChat(question, "faq".equals(intent), history);
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
            ChatResponse resp = chatClient.call(new Prompt(new UserMessage(prompt)));
            String r = resp.getResult().getOutput().getContent().trim().toLowerCase();
            if (r.contains("shop")) return "shop";
            if (r.contains("faq")) return "faq";
            return "chat";
        } catch (Exception e) {
            return "chat";
        }
    }

    /**
     * 购物意图：从商品库找匹配的
     */
    private ChatAnswer handleShop(String question) {
        // 把所有上架商品拼给 LLM，让它挑
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

        ChatResponse resp = chatClient.call(new Prompt(new UserMessage(prompt)));
        String answer = resp.getResult().getOutput().getContent();

        // 让 LLM 推荐的商品名做简单匹配，返回前端商品卡片
        List<String> matched = products.stream()
                .filter(p -> answer.contains(p.getProductName()))
                .map(p -> p.getId() + "|" + p.getProductName() + "|" + p.getPrice())
                .collect(Collectors.toList());

        return new ChatAnswer(answer, matched);
    }

    private ChatAnswer handleChat(String question, boolean isFaq, List<Map<String, String>> history) {
        String context = "";
        List<String> snippets = List.of();
        if (isFaq) {
            List<Document> relevantDocs = vectorStore.similaritySearch(
                    SearchRequest.query(question).withTopK(3)
            );
            if (relevantDocs != null && !relevantDocs.isEmpty()) {
                context = relevantDocs.stream().map(Document::getContent).collect(Collectors.joining("\n---\n"));
                snippets = relevantDocs.stream().map(Document::getContent).collect(Collectors.toList());
            }
        }

        // 拼接历史对话
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
        ChatResponse response = chatClient.call(prompt);
        return new ChatAnswer(response.getResult().getOutput().getContent(), snippets);
    }

    public record ChatAnswer(
            String answer,
            List<String> products  // 购物意图时返回 "id|name|price" 列表
    ) {}
}
