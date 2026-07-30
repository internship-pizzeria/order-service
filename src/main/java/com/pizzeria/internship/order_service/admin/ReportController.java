package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.infrastructure.ReportJob;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportOrchestrator;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportRequest;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportStatus;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reports")
class ReportController {

    private final ReportOrchestrator orchestrator;

    @PostMapping
    ResponseEntity<List<ReportJobResponse>> createReport(
            @RequestParam(required = false) List<ReportType> type,
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {

        if (type == null || type.isEmpty()) {
            type = List.of(ReportType.values());
        }

        List<ReportJobResponse> jobs = type.stream().map(t -> {
            AnalyticsScope scope = AdminAnalyticsController.resolveScope(locationId);
            ReportRequest request = new ReportRequest(scope, from, to);
            ReportJob job = orchestrator.submit(t, request);
            orchestrator.execute(job.getId());
            return ReportJobResponse.from(job);
        }).toList();

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}/status")
    ResponseEntity<ReportJobResponse> getStatus(@PathVariable UUID id) {
        ReportJob job = orchestrator.getJob(id);
        return ResponseEntity.ok(ReportJobResponse.from(job));
    }

    @GetMapping("/{id}/download")
    ResponseEntity<Resource> downloadReport(@PathVariable UUID id) {
        ReportJob job = orchestrator.getJob(id);

        if (job.getStatus() != ReportStatus.COMPLETED) {
            return ResponseEntity.status(202).build();
        }

        if (job.getFileContent() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = job.getFileContent().getBytes(StandardCharsets.UTF_8);
        Resource resource = new ByteArrayResource(content);

        String fileName = job.getType().name().toLowerCase() + "_report.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
