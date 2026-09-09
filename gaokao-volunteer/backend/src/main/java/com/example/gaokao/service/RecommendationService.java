
package com.example.gaokao.service;

import com.example.gaokao.dto.request.RecommendationRequest;
import com.example.gaokao.dto.response.RecommendationResponse;
import com.example.gaokao.entity.User;

public interface RecommendationService {

    RecommendationResponse generateRecommendations(User user, RecommendationRequest request);

    Double calculateAdmissionProbability(Integer userRank, Integer minRank, Integer maxRank, Integer avgRank);

    String determineRiskLevel(Double probability);

    Integer convertScoreToRank(String province, String year, Integer score, String scienceOrArts);
}
