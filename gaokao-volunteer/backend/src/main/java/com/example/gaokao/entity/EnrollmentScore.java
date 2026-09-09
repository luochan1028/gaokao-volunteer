
package com.example.gaokao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enrollment_scores")
public class EnrollmentScore {

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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScienceOrArts scienceOrArts;

    @Column(name = "min_score")
    private Integer minScore;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "average_score")
    private Integer averageScore;

    @Column(name = "min_rank")
    private Integer minRank;

    @Column(name = "max_rank")
    private Integer maxRank;

    @Column(name = "average_rank")
    private Integer averageRank;

    @Column(name = "enrollment_count")
    private Integer enrollmentCount;

    @Column(name = "major_name", length = 100)
    private String majorName;

    @Column(length = 50)
    private String majorCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    public enum Batch {
        BATCH_A, BATCH_B, BATCH_C, SPECIAL_BATCH, ART_BATCH, SPORTS_BATCH
    }

    public enum ScienceOrArts {
        SCIENCE, ARTS, BOTH, NEW_GAOKAO
    }
}
