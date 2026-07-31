package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.infrastructure.ReportJob;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportStatus;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A report generation job and its current status")
record ReportJobResponse(
        @Schema(description = "Unique identifier of the report job", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Type of the generated report")
        ReportType type,

        @Schema(description = "Current status of the job")
        ReportStatus status,

        @Schema(description = "Timestamp when the job was created", example = "2026-07-31T10:15:30Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the job finished (completed or failed)", example = "2026-07-31T10:15:31Z")
        Instant completedAt,

        @Schema(description = "True when the generated file is ready to be downloaded", example = "false")
        boolean hasFile,

        @Schema(description = "Error message when the job failed", example = "null")
        String errorMessage
) {
    static ReportJobResponse from(ReportJob job) {
        return new ReportJobResponse(
                job.getId(),
                job.getType(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getCompletedAt(),
                job.getFileContent() != null,
                job.getErrorMessage()
        );
    }
}
