package com.example.stallsystem.controller;

import com.example.stallsystem.ai.ComplaintClassifyService;
import com.example.stallsystem.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 投诉智能分类接口
 */
@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    private final ComplaintClassifyService complaintClassifyService;

    public ComplaintController(ComplaintClassifyService complaintClassifyService) {
        this.complaintClassifyService = complaintClassifyService;
    }

    /**
     * 投诉智能分类
     *
     * 请求体示例：
     * {
     *   "content": "那个卖臭豆腐的摊位油烟太大了，整个街都是臭的，而且垃圾乱扔，地上全是油"
     * }
     */
    @PostMapping("/classify")
    public Result<ComplaintClassifyService.ComplaintClassifyResult> classify(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isBlank()) {
            return Result.error("投诉内容不能为空");
        }
        try {
            ComplaintClassifyService.ComplaintClassifyResult result = complaintClassifyService.classify(content);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("投诉分类失败：" + e.getMessage());
        }
    }
}
