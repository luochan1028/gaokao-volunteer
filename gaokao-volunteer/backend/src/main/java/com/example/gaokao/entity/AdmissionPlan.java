package com.example.gaokao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admission_plans")
public class AdmissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String year;

    @Column(length = 20)
    private String province;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Batch batch;

    @Column(length = 20)
    private String scienceOrArts;

    @Column(name = "college_code", length = 20)
    private String collegeCode;

    @Column(name = "major_code", length = 20)
    private String majorCode;

    @Column(name = "major_name", length = 100)
    private String majorName;

    @Column(name = "enrollment_count")
    private Integer enrollmentCount;

    @Column(name = "school_system", length = 20)
    private String schoolSystem;

    @Column(name = "tuition")
    private Integer tuition;

    @Column(columnDefinition = "TEXT")
    private String subjectRequirements;

    @Column(name = "is_simulated")
    private Boolean isSimulated = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    public enum Batch {
        BATCH_A, BATCH_B, BATCH_C, SPECIAL_BATCH, EARLY_BATCH, ART_BATCH, SPORTS_BATCH
    }
}
