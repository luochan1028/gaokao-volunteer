
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
@Table(name = "majors")
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private MajorCategory category;

    @Column(length = 100)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String coreCourses;

    @Column(columnDefinition = "TEXT")
    private String employmentDirections;

    @Column(name = "satisfaction_score")
    private Double satisfactionScore;

    @Column(name = "average_salary")
    private Integer averageSalary;

    @Column(name = "employment_rate")
    private Double employmentRate;

    @Column(name = "is_hot")
    private Boolean isHot = false;

    @Column(name = "is_national_key")
    private Boolean isNationalKey = false;

    @Column(columnDefinition = "TEXT")
    private String subjectRequirements;

    @Column(columnDefinition = "TEXT")
    private String careerProspects;

    @Column(columnDefinition = "TEXT")
    private String relatedOccupations;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    public enum MajorCategory {
        PHILOSOPHY, ECONOMICS, LAW, EDUCATION, LITERATURE, HISTORY, SCIENCE, ENGINEERING, AGRICULTURE, MEDICINE, MANAGEMENT, ART
    }
}
