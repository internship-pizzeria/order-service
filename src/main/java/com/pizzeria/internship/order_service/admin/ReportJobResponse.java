package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.infrastructure.ReportJob;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportStatus;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;

import java.time.Instant;
import java.util.UUID;

record ReportJobResponse(
        UUID id,
        ReportType type,
        ReportStatus status,
        Instant createdAt,
        Instant completedAt,
        String filePath,
        String errorMessage
) {
    static ReportJobResponse from(ReportJob job) {
        return new ReportJobResponse(
                job.getId(),
                job.getType(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getCompletedAt(),
                job.getFilePath(),
                job.getErrorMessage()
        );
    }
}