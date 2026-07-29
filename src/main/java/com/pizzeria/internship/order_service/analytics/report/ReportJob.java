package com.pizzeria.internship.order_service.analytics.report;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "report_jobs")
public class ReportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column
    private Long locationId;

    @Column(nullable = false)
    private Instant fromTime;

    @Column(nullable = false)
    private Instant toTime;

    @Column(columnDefinition = "TEXT")
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    private Instant createdAt;

    @Column
    private Instant completedAt;

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }

    public void markCompleted(String filePath) {
        this.status = ReportStatus.COMPLETED;
        this.filePath = filePath;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }
}