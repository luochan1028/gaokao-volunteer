package com.example.gaokao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生画像 - V2.0 PRD核心数据模型
 * 存储对话采集的全部结构化信息
 */
@Entity
@Table(name = "student_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联用户
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // === 基础信息 ===
    @Column(name = "province")
    private String province;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "subject_comb")
    private String subjectComb;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "batch")
    private String batch;

    // 选填
    @Column(name = "single_subject_scores", length = 2000)
    private String singleSubjectScores; // JSON: {"物理":85,"化学":78}

    @Column(name = "art_sport")
    private Boolean artSport;

    @Column(name = "physical_limit", length = 500)
    private String physicalLimit; // JSON: ["色盲","身高不足"]

    @Column(name = "preferred_city_tier", length = 500)
    private String preferredCityTier; // JSON: ["一线","新一线"]

    // === 张雪峰家庭现实八问 ===
    @Column(name = "economy_type")
    private String economyType; // 宽松型/承压型

    @Column(name = "resource_type")
    private String resourceType; // 资源型/无资源型

    @Column(name = "strategy_type")
    private String strategyType; // 名校导向/专业导向/兜底导向

    @Column(name = "gender")
    private String gender; // 男/女/不愿透露

    @Column(name = "social_preference")
    private String socialPreference; // 内向/外向/中间

    @Column(name = "postgrad_intent")
    private String postgradIntent; // 考研导向/就业导向/未定

    @Column(name = "city_preference")
    private String cityPreference; // 一线/新一线/老家/不确定

    @Column(name = "parent_value")
    private String parentValue; // 稳定/高薪/兴趣/名校平台

    // === 张雪峰专业筛选八问 ===
    @Column(name = "medical")
    private String medical; // 白名单/黑名单/待定

    @Column(name = "agriculture")
    private String agriculture;

    @Column(name = "normal")
    private String normal;

    @Column(name = "liberal_arts_major")
    private String liberalArtsMajor; // 接受/排除/偏好

    @Column(name = "chemistry_related")
    private String chemistryRelated; // 接受/排除

    @Column(name = "physics_related")
    private String physicsRelated; // 接受/排除

    @Column(name = "engineering_sub")
    private String engineeringSub; // 机械类/电气类/排除

    @Column(name = "math_major")
    private String mathMajor; // 白名单/黑名单/谨慎

    // === 霍兰德测评结果 ===
    @Column(name = "holland_primary")
    private String hollandPrimary; // R/I/A/S/E/C

    @Column(name = "holland_secondary")
    private String hollandSecondary;

    @Column(name = "holland_scores", length = 500)
    private String hollandScores; // JSON: {"R":3,"I":2,...}

    @Column(name = "holland_confidence")
    private String hollandConfidence; // high/low

    // === 职业路径偏好 ===
    @Column(name = "career_preferences", length = 2000)
    private String careerPreferences; // JSON array

    // === 权重偏好 ===
    @Column(name = "priority_weights", length = 500)
    private String priorityWeights; // JSON: {"school":0.3,"major":0.4,"city":0.3}

    // === 对话状态 ===
    @Column(name = "dialog_phase")
    private String dialogPhase; // idle/base_info/family_q/major_q/holland/career/generate/report

    @Column(name = "dialog_turns", length = 4000)
    private String dialogTurns; // JSON: 对话历史

    @Column(name = "collected_slots", length = 4000)
    private String collectedSlots; // JSON: 已采集的槽位

    @Column(name = "current_question_id")
    private String currentQuestionId; // 当前问题编号

    // === 意向书 ===
    @Column(name = "report_version")
    private Integer reportVersion;

    @Column(name = "report_data", length = 10000)
    private String reportData; // JSON: 意向书全文

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
