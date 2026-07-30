package com.pizzeria.internship.order_service.analytics.infrastructure;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportJob {

    private UUID id;
    private ReportType type;
    private ReportStatus status;
    private Long locationId;
    private Instant fromTime;
    private Instant toTime;
    private String filePath;
    private String fileContent;
    private String errorMessage;
    private Instant createdAt;
    private Instant completedAt;

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }

    public void markCompleted(String fileContent) {
        this.status = ReportStatus.COMPLETED;
        this.fileContent = fileContent;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }
}
