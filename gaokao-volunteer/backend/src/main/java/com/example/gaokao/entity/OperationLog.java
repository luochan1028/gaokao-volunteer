
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
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationType operationType;

    @Column(nullable = false, length = 200)
    private String operationDescription;

    @Column(length = 100)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationResult result;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum OperationType {
        LOGIN, LOGOUT, CREATE, READ, UPDATE, DELETE, SEARCH, EXPORT, IMPORT, RECOMMEND, ASSESSMENT
    }

    public enum OperationResult {
        SUCCESS, FAILURE, PARTIAL
    }
}
