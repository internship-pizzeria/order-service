package com.pizzeria.internship.order_service.analytics.fulfillment;

import com.pizzeria.internship.order_service.analytics.infrastructure.ReportRequest;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FulfillmentReportGeneratorTest {

    @Mock
    private FulfillmentService fulfillmentService;

    @InjectMocks
    private FulfillmentReportGenerator generator;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");

    @Test
    void shouldReturnCorrectType() {
        assertEquals(ReportType.FULFILLMENT, generator.getType());
    }

    @Test
    void shouldReturnCorrectHeader() {
        assertEquals("from_status,to_status,average_minutes,median_minutes,p95_minutes", generator.getHeader());
    }

    @Test
    void shouldGenerateCsvWithSummaryAndPerStatusRows() {
        List<FulfillmentMetricsResponse.StatusTiming> timings = List.of(
                new FulfillmentMetricsResponse.StatusTiming("NEW", "ACCEPTED", 2.0, 1.5, 5.0),
                new FulfillmentMetricsResponse.StatusTiming("ACCEPTED", "IN_PROGRESS", 8.0, 7.0, 15.0)
        );

        when(fulfillmentService.calculateMetrics(5L, FROM, TO))
                .thenReturn(new FulfillmentMetricsResponse(45.5, 40.0, 90.0, timings));

        ReportRequest request = new ReportRequest(new SingleLocation(5L), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals(3, rows.size());
        assertEquals("NEW,DELIVERED,45.5,40.0,90.0", rows.get(0));
        assertEquals("NEW,ACCEPTED,2.0,1.5,5.0", rows.get(1));
        assertEquals("ACCEPTED,IN_PROGRESS,8.0,7.0,15.0", rows.get(2));
    }

    @Test
    void shouldGenerateCsvWithOnlySummaryWhenNoPerStatusData() {
        when(fulfillmentService.calculateMetrics(null, FROM, TO))
                .thenReturn(new FulfillmentMetricsResponse(50.0, 45.0, 95.0, List.of()));

        ReportRequest request = new ReportRequest(new AllLocations(), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals(1, rows.size());
        assertEquals("NEW,DELIVERED,50.0,45.0,95.0", rows.getFirst());
    }

    @Test
    void shouldEscapeCsvValuesWithSpecialCharacters() {
        List<FulfillmentMetricsResponse.StatusTiming> timings = List.of(
                new FulfillmentMetricsResponse.StatusTiming("NEW", "ACC,EPTED", 2.5, 2.0, 6.0)
        );

        when(fulfillmentService.calculateMetrics(null, FROM, TO))
                .thenReturn(new FulfillmentMetricsResponse(30.0, 28.0, 55.0, timings));

        ReportRequest request = new ReportRequest(new AllLocations(), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals(2, rows.size());
        assertEquals("NEW,DELIVERED,30.0,28.0,55.0", rows.get(0));
        assertEquals("NEW,\"ACC,EPTED\",2.5,2.0,6.0", rows.get(1));
    }
}
