package com.example.gaokao.dto.response;

import lombok.Data;
import java.util.List;

/**
 * V2.0 志愿推荐方案项
 */
@Data
public class RecommendationItem {
    private String schoolName;
    private String majorGroup;
    private List<String> majorList;
    private Integer matchScore;
    private String admissionProb;    // 冲/稳/保
    private String realityReason;
    private String personalityReason;
    private String careerReason;
    private List<String> riskWarnings;
    private String oneSentence;
}
