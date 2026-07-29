package com.pizzeria.internship.order_service.analytics.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class ReportJobRepository {

    private final JdbcTemplate analyticsJdbcTemplate;

    ReportJobRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    ReportJob save(ReportJob job) {
        if (job.getId() == null) {
            job.setId(UUID.randomUUID());
            insert(job);
        } else {
            update(job);
        }
        return job;
    }

    Optional<ReportJob> findById(UUID id) {
        return analyticsJdbcTemplate.query("""
                SELECT id, type, status, location_id, from_time, to_time,
                       file_path, error_message, created_at, completed_at
                FROM report_jobs
                WHERE id = ?
                """, ROW_MAPPER, id).stream().findFirst();
    }

    private void insert(ReportJob job) {
        Instant now = Instant.now();
        analyticsJdbcTemplate.update("""
                INSERT INTO report_jobs
                (id, type, status, location_id, from_time, to_time, file_path, error_message, created_at, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                job.getId(),
                job.getType().name(),
                job.getStatus().name(),
                job.getLocationId(),
                Timestamp.from(job.getFromTime()),
                Timestamp.from(job.getToTime()),
                job.getFilePath(),
                job.getErrorMessage(),
                Timestamp.from(now),
                job.getCompletedAt() != null ? Timestamp.from(job.getCompletedAt()) : null
        );
        job.setCreatedAt(now);
    }

    private void update(ReportJob job) {
        analyticsJdbcTemplate.update("""
                UPDATE report_jobs
                SET status = ?, file_path = ?, error_message = ?, completed_at = ?
                WHERE id = ?
                """,
                job.getStatus().name(),
                job.getFilePath(),
                job.getErrorMessage(),
                job.getCompletedAt() != null ? Timestamp.from(job.getCompletedAt()) : null,
                job.getId()
        );
    }

    private static final RowMapper<ReportJob> ROW_MAPPER = (ResultSet rs, int rowNum) -> ReportJob.builder()
            .id(UUID.fromString(rs.getString("id")))
            .type(ReportType.valueOf(rs.getString("type")))
            .status(ReportStatus.valueOf(rs.getString("status")))
            .locationId((Long) rs.getObject("location_id"))
            .fromTime(rs.getTimestamp("from_time").toInstant())
            .toTime(rs.getTimestamp("to_time").toInstant())
            .filePath(rs.getString("file_path"))
            .errorMessage(rs.getString("error_message"))
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .completedAt(rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toInstant() : null)
            .build();
}
