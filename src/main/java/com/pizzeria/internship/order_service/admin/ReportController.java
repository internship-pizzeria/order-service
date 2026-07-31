package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.infrastructure.ReportJob;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportOrchestrator;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportRequest;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportStatus;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Admin Reports", description = "On-demand CSV report generation (revenue, popularity, fulfillment, " +
        "location performance, peak hours). All endpoints require the 'X-User-Id' and 'LocationId' HTTP headers.")
class ReportController {

    private final ReportOrchestrator orchestrator;

    @PostMapping
    @Operation(
            summary = "Generate reports",
            description = "Submits and immediately executes report jobs for the requested report types and time range. " +
                    "When no 'type' is provided, one job per supported report type is created. Returns the created " +
                    "jobs together with their status so they can be polled and downloaded.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report jobs created and executed"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<List<ReportJobResponse>> createReport(
            @Parameter(description = "Report types to generate. When omitted, all supported types are generated.",
                    schema = @Schema(implementation = ReportType.class))
            @RequestParam(required = false) List<ReportType> type,
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
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
    @Operation(
            summary = "Get report job status",
            description = "Returns the current status of a report job. Poll this endpoint until the job reaches " +
                    "COMPLETED before downloading the file.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report job found"),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Report job with the given ID does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<ReportJobResponse> getStatus(
            @Parameter(description = "Unique identifier of the report job", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        ReportJob job = orchestrator.getJob(id);
        return ResponseEntity.ok(ReportJobResponse.from(job));
    }

    @GetMapping("/{id}/download")
    @Operation(
            summary = "Download report file",
            description = "Downloads the generated CSV file of a completed report job. Returns 202 while the report " +
                    "is still being processed.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file content, delivered as an attachment",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "202", description = "Report is still being processed", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Report job not found or completed without content",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<Resource> downloadReport(
            @Parameter(description = "Unique identifier of the report job", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
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
