package com.pizzeria.internship.order_service.analytics.peakhours;

import com.pizzeria.internship.order_service.admin.AdminAnalyticsDtos;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportRequest;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeakHoursReportGeneratorTest {

    @Mock
    private PeakHoursService peakHoursService;

    @InjectMocks
    private PeakHoursReportGenerator generator;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");

    @Test
    void shouldReturnCorrectType() {
        assertEquals(ReportType.PEAK_HOURS, generator.getType());
    }

    @Test
    void shouldReturnCorrectHeader() {
        assertEquals("hour,order_count,revenue,is_peak", generator.getHeader());
    }

    @Test
    void shouldGenerateCsvRows() {
        List<AdminAnalyticsDtos.PeakHourItem> hours = List.of(
                new AdminAnalyticsDtos.PeakHourItem(8, 2, BigDecimal.valueOf(40), false),
                new AdminAnalyticsDtos.PeakHourItem(12, 20, BigDecimal.valueOf(400), true)
        );

        when(peakHoursService.getPeakHours(new SingleLocation(5L), FROM, TO))
                .thenReturn(new AdminAnalyticsDtos.PeakHoursResponse(hours));

        ReportRequest request = new ReportRequest(new SingleLocation(5L), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals(2, rows.size());
        assertEquals("8,2,40,false", rows.get(0));
        assertEquals("12,20,400,true", rows.get(1));
    }

    @Test
    void shouldGenerateAll24RowsWhenFullResponse() {
        List<AdminAnalyticsDtos.PeakHourItem> allHours = List.of(
                new AdminAnalyticsDtos.PeakHourItem(0, 0, BigDecimal.ZERO, false),
                new AdminAnalyticsDtos.PeakHourItem(1, 0, BigDecimal.ZERO, false)
        );

        when(peakHoursService.getPeakHours(new AllLocations(), FROM, TO))
                .thenReturn(new AdminAnalyticsDtos.PeakHoursResponse(allHours));

        ReportRequest request = new ReportRequest(new AllLocations(), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals(2, rows.size());
    }

    @Test
    void shouldEscapeSpecialCharacters() {
        List<AdminAnalyticsDtos.PeakHourItem> hours = List.of(
                new AdminAnalyticsDtos.PeakHourItem(8, 1000, BigDecimal.valueOf(9999.99), true)
        );

        when(peakHoursService.getPeakHours(new AllLocations(), FROM, TO))
                .thenReturn(new AdminAnalyticsDtos.PeakHoursResponse(hours));

        ReportRequest request = new ReportRequest(new AllLocations(), FROM, TO);
        List<String> rows = generator.generate(request);

        assertEquals("8,1000,9999.99,true", rows.get(0));
    }
}
