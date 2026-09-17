package com.example.gaokao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 院校专业组 - V2.0 志愿生成引擎核心数据
 */
@Entity
@Table(name = "school_major_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolMajorGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "school_code")
    private String schoolCode;

    @Column(name = "school_province")
    private String schoolProvince;

    @Column(name = "school_tier")
    private String schoolTier; // 985/211/双一流/省重点/普通本科/民办

    @Column(name = "major_group_code")
    private String majorGroupCode;

    @Column(name = "major_group_name")
    private String majorGroupName;

    @Column(name = "major_list", length = 1000)
    private String majorList; // JSON: ["计算机科学","软件工程"]

    @Column(name = "subject_requirement")
    private String subjectRequirement; // 选科要求: "物理+化学" / "历史+不限"

    @Column(name = "batch")
    private String batch; // 本科批/专科批/提前批

    @Column(name = "admission_line_2024")
    private Integer admissionLine2024;

    @Column(name = "admission_line_2023")
    private Integer admissionLine2023;

    @Column(name = "admission_line_2022")
    private Integer admissionLine2022;

    @Column(name = "min_rank_2024")
    private Integer minRank2024;

    @Column(name = "min_rank_2023")
    private Integer minRank2023;

    @Column(name = "min_rank_2022")
    private Integer minRank2022;

    @Column(name = "has_master_point")
    private Boolean hasMasterPoint;

    @Column(name = "tuition_fee")
    private Integer tuitionFee;

    @Column(name = "is_discontinued")
    private Boolean isDiscontinued;

    @Column(name = "is_changed")
    private Boolean isChanged;

    @Column(name = "data_insufficient")
    private Boolean dataInsufficient;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
