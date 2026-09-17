package com.example.gaokao.controller;

import com.example.gaokao.dto.request.DialogRequest;
import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.dto.response.DialogResponse;
import com.example.gaokao.dto.response.IntentionReport;
import com.example.gaokao.service.DialogEngineService;
import com.example.gaokao.service.IntentionEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * V2.0 AI对话 + 志愿意向 接口
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAssistantController {

    private final DialogEngineService dialogService;
    private final IntentionEngineService intentionService;

    /**
     * 开始对话
     */
    @PostMapping("/dialog/start")
    public ApiResponse<DialogResponse> startDialog(@RequestParam Long userId) {
        DialogResponse resp = dialogService.startDialog(userId);
        return ApiResponse.success("对话已开始", resp);
    }

    /**
     * 发送消息
     */
    @PostMapping("/dialog/message")
    public ApiResponse<DialogResponse> sendMessage(@RequestBody DialogRequest req) {
        DialogResponse resp = dialogService.processMessage(req);
        return ApiResponse.success("处理成功", resp);
    }

    /**
     * 获取张雪峰家庭八问
     */
    @GetMapping("/questions/family")
    public ApiResponse<?> getFamilyQuestions() {
        return ApiResponse.success(com.example.gaokao.data.ZhangXuefengQuestions.FAMILY_QUESTIONS);
    }

    /**
     * 获取张雪峰专业八问
     */
    @GetMapping("/questions/major")
    public ApiResponse<?> getMajorQuestions() {
        return ApiResponse.success(com.example.gaokao.data.ZhangXuefengQuestions.MAJOR_QUESTIONS);
    }

    /**
     * 获取霍兰德12题
     */
    @GetMapping("/questions/holland")
    public ApiResponse<?> getHollandQuestions() {
        return ApiResponse.success(com.example.gaokao.data.HollandQuestions.QUESTIONS);
    }

    /**
     * 生成志愿意向书
     */
    @PostMapping("/report/generate")
    public ApiResponse<IntentionReport> generateReport(@RequestParam Long userId) {
        IntentionReport report = intentionService.generateReport(userId);
        if (report == null) {
            return ApiResponse.error("未找到用户画像数据");
        }
        return ApiResponse.success("意向书已生成", report);
    }

    /**
     * 获取已生成的志愿意向书
     */
    @GetMapping("/report/{userId}")
    public ApiResponse<IntentionReport> getReport(@PathVariable Long userId) {
        IntentionReport report = intentionService.generateReport(userId);
        if (report == null) {
            return ApiResponse.error("未找到用户画像数据");
        }
        return ApiResponse.success("获取成功", report);
    }
}
