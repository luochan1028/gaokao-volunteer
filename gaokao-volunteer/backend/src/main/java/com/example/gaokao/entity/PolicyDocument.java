
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
@Table(name = "policy_documents")
public class PolicyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 20)
    private String province;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PolicyType policyType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String source;

    @Column(length = 255)
    private String url;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PolicyType {
        EXAM_POLICY, ENROLLMENT_RULE, MAJOR_INTRO, SCORE_LINE, EMPLOYMENT_REPORT, OTHER
    }
}
