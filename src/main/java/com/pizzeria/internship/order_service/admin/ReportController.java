package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.AnalyticsScope;
import com.pizzeria.internship.order_service.analytics.report.ReportJob;
import com.pizzeria.internship.order_service.analytics.report.ReportOrchestrator;
import com.pizzeria.internship.order_service.analytics.report.ReportRequest;
import com.pizzeria.internship.order_service.analytics.report.ReportStatus;
import com.pizzeria.internship.order_service.analytics.report.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reports")
class ReportController {

    private final ReportOrchestrator orchestrator;

    @PostMapping
    ResponseEntity<ReportJobResponse> createReport(
            @RequestParam ReportType type,
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {

        AnalyticsScope scope = AdminAnalyticsController.resolveScope(locationId);
        ReportRequest request = new ReportRequest(scope, from, to);
        ReportJob job = orchestrator.submit(type, request);
        orchestrator.execute(job.getId());

        ReportJobResponse response = ReportJobResponse.from(job);
        return ResponseEntity.created(
                        URI.create("/api/v1/admin/reports/" + job.getId()))
                .body(response);
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

        if (job.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(Path.of(job.getFilePath()));
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String fileName = job.getType().name().toLowerCase() + "_report.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
