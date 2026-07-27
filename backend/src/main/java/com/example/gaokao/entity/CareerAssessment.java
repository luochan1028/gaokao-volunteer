
package com.example.gaokao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "career_assessments")
public class CareerAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssessmentType assessmentType;

    @Column(columnDefinition = "TEXT")
    private String answers;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String personalityAnalysis;

    @Column(columnDefinition = "TEXT")
    private String interestAreas;

    @Column(columnDefinition = "TEXT")
    private String suggestedMajors;

    @Column(columnDefinition = "TEXT")
    private String suggestedCareers;

    @Column(columnDefinition = "TEXT")
    private String report;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum AssessmentType {
        INTEREST, PERSONALITY, ABILITY, VALUES, COMPREHENSIVE
    }
}
