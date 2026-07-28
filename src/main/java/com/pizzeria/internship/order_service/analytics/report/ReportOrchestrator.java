package com.pizzeria.internship.order_service.analytics.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ReportOrchestrator.class);
    private static final String REPORTS_DIR = "reports";

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
                .locationId(request.locationId())
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
            ReportRequest request = new ReportRequest(
                    job.getLocationId(),
                    job.getFromTime(),
                    job.getToTime()
            );

            List<String> rows = generator.generate(request);
            Path dir = Path.of(REPORTS_DIR);
            Files.createDirectories(dir);
            String fileName = job.getType().name().toLowerCase() + "_" + jobId + ".csv";
            Path filePath = dir.resolve(fileName);

            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile()))) {
                writer.println(generator.getHeader());
                rows.forEach(writer::println);
            }

            job.markCompleted(filePath.toString());
            jobRepository.save(job);
            log.info("Report {} completed: {} rows", jobId, rows.size());
        } catch (Exception e) {
            log.error("Report {} failed", jobId, e);
            job.markFailed(e.getMessage());
            jobRepository.save(job);
        }
    }
}