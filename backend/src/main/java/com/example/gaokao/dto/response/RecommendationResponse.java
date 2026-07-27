
package com.example.gaokao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private List<RecommendationItem> chongList;

    private List<RecommendationItem> wenList;

    private List<RecommendationItem> baoList;

    private RiskAssessment riskAssessment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Long collegeId;
        private String collegeName;
        private String collegeProvince;
        private String collegeCity;
        private Boolean is985;
        private Boolean is211;
        private Boolean isDoubleFirstClass;
        private Long majorId;
        private String majorName;
        private String majorCategory;
        private String subjectRequirements;
        private Integer lastYearMinScore;
        private Integer lastYearMaxScore;
        private Integer lastYearAverageScore;
        private Integer lastYearMinRank;
        private Integer lastYearMaxRank;
        private Integer lastYearAverageRank;
        private Double probability;
        private String riskLevel;
        private Integer priorityOrder;
        private String gradientType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAssessment {
        private Double slipProbability;
        private Double wasteScoreIndex;
        private String riskLevel;
        private String suggestions;
        private Integer totalRecommended;
        private Integer chongCount;
        private Integer wenCount;
        private Integer baoCount;
    }
}
