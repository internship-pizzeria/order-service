package com.pizzeria.internship.order_service.analytics.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ReportJobRepository extends JpaRepository<ReportJob, UUID> {
}