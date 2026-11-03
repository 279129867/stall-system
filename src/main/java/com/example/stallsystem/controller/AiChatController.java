package com.example.stallsystem.controller;

import com.example.stallsystem.ai.AiChatService;
import com.example.stallsystem.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/ask")
    public Result<AiChatService.ChatAnswer> ask(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        if (question == null || question.isBlank()) {
            return Result.error("问题不能为空");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        try {
            AiChatService.ChatAnswer answer = aiChatService.ask(question, history);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("AI 服务调用失败：" + e.getMessage());
        }
    }
}
