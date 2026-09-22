
package com.example.gaokao.entity;

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
@Table(name = "plan_items")
public class PlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VolunteerList.GradientType gradientType;

    @Column(name = "probability")
    private Double probability;

    @Column(name = "is_adjusted")
    private Boolean isAdjusted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private VolunteerPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_list_id")
    private VolunteerList volunteerList;
}
