
package com.example.gaokao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "colleges")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String shortName;

    @Column(length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CollegeType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CollegeLevel level;

    @Column(length = 50)
    private String province;

    @Column(length = 50)
    private String city;

    @Column(length = 100)
    private String address;

    @Column(length = 20)
    private String foundedYear;

    @Column(length = 200)
    private String description;

    @Column(length = 50)
    private String website;

    @Column(length = 50)
    private String phone;

    @Column(name = "satisfaction_score")
    private Double satisfactionScore;

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "teacher_count")
    private Integer teacherCount;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(columnDefinition = "TEXT")
    private String advantages;

    @Column(name = "is_985")
    private Boolean is985 = false;

    @Column(name = "is_211")
    private Boolean is211 = false;

    @Column(name = "is_double_first_class")
    private Boolean isDoubleFirstClass = false;

    @Column(name = "has_graduate_program")
    private Boolean hasGraduateProgram = false;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code")
    private Province provinceEntity;

    @JsonIgnore
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL)
    private List<Major> majors = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL)
    private List<EnrollmentScore> enrollmentScores = new ArrayList<>();

    public enum CollegeType {
        PUBLIC, PRIVATE, INDEPENDENT, VOCATIONAL
    }

    public enum CollegeLevel {
        UNIVERSITY, COLLEGE, INSTITUTE, VOCATIONAL_COLLEGE
    }
}
