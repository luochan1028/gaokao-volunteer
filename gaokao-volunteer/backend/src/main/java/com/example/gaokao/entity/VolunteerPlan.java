
package com.example.gaokao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "volunteer_plans")
public class VolunteerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SelectionMode selectionMode;

    @Column(name = "chong_count")
    private Integer chongCount;

    @Column(name = "wen_count")
    private Integer wenCount;

    @Column(name = "bao_count")
    private Integer baoCount;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "slip_probability")
    private Double slipProbability;

    @Column(name = "waste_score_index")
    private Double wasteScoreIndex;

    @Column(columnDefinition = "TEXT")
    private String analysisReport;

    @Column(columnDefinition = "TEXT")
    private String riskTips;

    @Column(columnDefinition = "TEXT")
    private String strategySuggestion;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlanItem> planItems = new ArrayList<>();

    public enum SelectionMode {
        COLLEGE_FIRST, MAJOR_FIRST
    }
}
