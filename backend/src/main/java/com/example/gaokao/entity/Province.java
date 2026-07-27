
package com.example.gaokao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provinces")
public class Province {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ExamMode examMode;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "subject_count")
    private Integer subjectCount;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<College> colleges = new ArrayList<>();

    public enum ExamMode {
        NEW_GAOKAO, TRADITIONAL
    }
}
