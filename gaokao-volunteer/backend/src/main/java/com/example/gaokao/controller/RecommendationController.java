
package com.example.gaokao.controller;

import com.example.gaokao.dto.request.RecommendationRequest;
import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.dto.response.RecommendationResponse;
import com.example.gaokao.entity.User;
import com.example.gaokao.service.RecommendationService;
import com.example.gaokao.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @PostMapping
    public ApiResponse<RecommendationResponse> generateRecommendations(@RequestBody RecommendationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);

        log.info("生成志愿推荐: 用户={}, 省份={}, 分数={}, 位次={}", phone, request.getProvince(), request.getScore(), request.getRank());
        RecommendationResponse response = recommendationService.generateRecommendations(user, request);
        return ApiResponse.success("推荐成功", response);
    }

    @GetMapping("/probability")
    public ApiResponse<Double> calculateProbability(
            @RequestParam Integer userRank,
            @RequestParam Integer minRank,
            @RequestParam Integer maxRank,
            @RequestParam(required = false) Integer avgRank) {
        Double probability = recommendationService.calculateAdmissionProbability(userRank, minRank, maxRank, avgRank);
        return ApiResponse.success(probability);
    }

    @GetMapping("/risk-level")
    public ApiResponse<String> determineRiskLevel(@RequestParam Double probability) {
        String riskLevel = recommendationService.determineRiskLevel(probability);
        return ApiResponse.success(riskLevel);
    }

    @GetMapping("/score-to-rank")
    public ApiResponse<Integer> convertScoreToRank(
            @RequestParam String province,
            @RequestParam String year,
            @RequestParam Integer score,
            @RequestParam String scienceOrArts) {
        Integer rank = recommendationService.convertScoreToRank(province, year, score, scienceOrArts);
        return ApiResponse.success(rank);
    }
}
