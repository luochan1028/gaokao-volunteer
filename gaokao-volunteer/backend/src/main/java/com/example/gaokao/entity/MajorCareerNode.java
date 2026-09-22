package com.example.gaokao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 专业职业知识图谱节点
 * 专业 -> 典型岗位 -> 行业 -> 起薪 -> 5年路径 -> 考研/考公 -> AI替代风险
 */
@Entity
@Table(name = "major_career_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MajorCareerNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "major_name")
    private String majorName;

    @Column(name = "major_code")
    private String majorCode;

    @Column(name = "category")
    private String category; // 工学/理学/医学/文学/法学/经济学/管理学/教育学/农学/艺术学

    @Column(name = "typical_jobs", length = 1000)
    private String typicalJobs; // JSON: ["软件开发工程师","算法工程师","运维工程师"]

    @Column(name = "industry_distribution", length = 1000)
    private String industryDistribution; // JSON: {"互联网":40,"金融":15,"制造业":20}

    @Column(name = "salary_tier1", length = 100)
    private String salaryTier1; // 一线城市: "8-15K"

    @Column(name = "salary_tier2", length = 100)
    private String salaryTier2; // 新一线: "6-10K"

    @Column(name = "salary_tier3", length = 100)
    private String salaryTier3; // 其他: "4-7K"

    @Column(name = "salary_5year_median", length = 100)
    private String salary5YearMedian; // "15-25K"

    @Column(name = "career_path_5year", length = 2000)
    private String careerPath5Year; // JSON: 5年发展路径

    @Column(name = "postgrad_directions", length = 1000)
    private String postgradDirections; // JSON: 考研方向

    @Column(name = "civil_service_jobs", length = 1000)
    private String civilServiceJobs; // JSON: 考公岗位映射

    @Column(name = "required_certs", length = 500)
    private String requiredCerts; // JSON: 所需证书

    @Column(name = "ai_replace_risk")
    private String aiReplaceRisk; // 高/中/低

    @Column(name = "reality_tag")
    private String realityTag; // 天坑/风口/普通家庭慎选/常规

    @Column(name = "holland_code")
    private String hollandCode; // RIASEC匹配

    @Column(name = "reality_reason")
    private String realityReason; // 现实评价说明

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
