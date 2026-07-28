package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.report.ReportOrchestrator;
import com.pizzeria.internship.order_service.analytics.report.ReportJob;
import com.pizzeria.internship.order_service.analytics.report.ReportRequest;
import com.pizzeria.internship.order_service.analytics.report.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

        ReportRequest request = new ReportRequest(locationId, from, to);
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
}