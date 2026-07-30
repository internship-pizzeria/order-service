package com.pizzeria.internship.order_service.analytics.infrastructure;

import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ReportOrchestrator.class);
    private final ReportRegistry registry;
    private final ReportJobRepository jobRepository;

    public ReportJob getJob(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    }

    public ReportJob submit(ReportType type, ReportRequest request) {
        ReportJob job = ReportJob.builder()
                .type(type)
                .status(ReportStatus.PENDING)
                .locationId(request.scope().extractLocationId())
                .fromTime(request.from())
                .toTime(request.to())
                .build();
        jobRepository.save(job);
        return job;
    }

    @Async
    public void execute(UUID jobId) {
        ReportJob job = jobRepository.findById(jobId).orElseThrow();
        job.updateStatus(ReportStatus.RUNNING);
        jobRepository.save(job);

        try {
            ReportGenerator generator = registry.getGenerator(job.getType());
            ReportRequest request = buildRequest(job);

            List<String> rows = generator.generate(request);

            String header = generator.getHeader();
            String csvContent = rows.isEmpty() ? header : header + "\n" + String.join("\n", rows);

            job.markCompleted(csvContent);
            jobRepository.save(job);
            log.info("Report {} completed: {} rows", jobId, rows.size());
        } catch (Exception e) {
            log.error("Report {} failed", jobId, e);
            job.markFailed(e.getMessage());
            jobRepository.save(job);
        }
    }

    private static ReportRequest buildRequest(ReportJob job) {
        AnalyticsScope scope =
                job.getLocationId() != null
                        ? new SingleLocation(job.getLocationId())
                        : new AllLocations();
        return new ReportRequest(scope, job.getFromTime(), job.getToTime());
    }
}
