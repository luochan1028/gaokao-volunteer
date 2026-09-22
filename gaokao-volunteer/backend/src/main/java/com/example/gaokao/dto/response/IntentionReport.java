package com.example.gaokao.dto.response;

import lombok.Data;
import java.util.List;

/**
 * V2.0 志愿意向书 - 完整输出
 */
@Data
public class IntentionReport {
    private Integer version;
    private String timestamp;
    private String dataVersion;
    private String disclaimer;

    // 章节内容
    private String studentSummary;       // 1. 学生画像摘要
    private String familyConclusion;      // 2. 家庭现实结论
    private String personalityResult;     // 3. 性格与职业倾向
    private List<RecommendationItem> recommendedMajors; // 4. 推荐专业TOP10
    private List<String> cautiousMajors;  // 5. 慎选专业TOP5
    private List<RecommendationItem> chongPlans;  // 6. 冲方案
    private List<RecommendationItem> wenPlans;    // 6. 稳方案
    private List<RecommendationItem> baoPlans;    // 6. 保方案
    private String cityAnalysis;          // 7. 城市与路径分析
    private List<String> conflictPoints;  // 8. 家长vs学生冲突点
    private List<String> pendingQuestions;// 9. 待确认问题清单
}
