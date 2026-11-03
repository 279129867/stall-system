package com.example.stallsystem.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 投诉智能分类服务
 *
 * 利用大模型对用户提交的投诉文本自动分类，
 * 输出：类别、严重程度、摘要、建议处理方式
 */
@Slf4j
@Service
public class ComplaintClassifyService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ComplaintClassifyService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 投诉分类 Prompt
     */
    private static final String CLASSIFY_SYSTEM_PROMPT = """
            你是一个摆摊管理系统的投诉自动分类助手。请分析用户提交的投诉内容，并输出结构化的 JSON 结果。
            
            分类类别（category 只能选以下之一）：
            - 卫生问题：垃圾、油污、环境脏乱等
            - 噪音扰民：喇叭、音响、噪音过大等
            - 食品安全：变质食品、卫生不达标、吃出异物等
            - 服务态度：摊主态度差、吵架、不配合管理等
            - 价格纠纷：乱收费、缺斤短两等
            - 其他：不属于以上类别的
            
            严重程度（severity）分为三级：
            - 轻微：偶发、影响小，警告即可
            - 一般：多次发生或影响中等，需要罚款
            - 严重：涉及食品安全、违法违规或造成恶劣影响，需要停业整顿
            
            请严格按照以下 JSON 格式输出，不要输出其他内容：
            {
              "category": "分类类别",
              "severity": "严重程度",
              "summary": "一句话摘要（不超过30字）",
              "suggestion": "建议处理方式"
            }
            """;

    /**
     * 对投诉文本进行智能分类
     */
    public ComplaintClassifyResult classify(String complaintText) {
        log.info("=== 收到投诉文本，开始分类：{} ===", complaintText);

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage(CLASSIFY_SYSTEM_PROMPT),
                        new UserMessage("投诉内容：" + complaintText)
                )
        );

        String rawResult = chatClient.call(prompt).getResult().getOutput().getContent();
        log.info("=== 大模型原始输出：{} ===", rawResult);

        try {
            // 尝试解析 JSON
            // 有些模型会用 ```json ... ``` 包裹，先清理一下
            String cleaned = rawResult.trim()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "");

            @SuppressWarnings("unchecked")
            Map<String, String> map = objectMapper.readValue(cleaned, Map.class);

            return new ComplaintClassifyResult(
                    map.getOrDefault("category", "其他"),
                    map.getOrDefault("severity", "一般"),
                    map.getOrDefault("summary", complaintText.length() > 30 ? complaintText.substring(0, 30) + "..." : complaintText),
                    map.getOrDefault("suggestion", "请管理员核实处理")
            );
        } catch (Exception e) {
            log.error("=== 解析分类结果失败，使用原始文本作为结果 ===", e);
            return new ComplaintClassifyResult("其他", "一般", complaintText, "请管理员人工核实处理");
        }
    }

    /**
     * 投诉分类结果
     */
    public record ComplaintClassifyResult(
            String category,      // 投诉类别
            String severity,      // 严重程度
            String summary,       // 摘要
            String suggestion     // 建议处理方式
    ) {}
}
