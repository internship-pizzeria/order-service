package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import com.pizzeria.internship.order_service.admin.ProductRankingResponse;
import com.pizzeria.internship.order_service.admin.RankingSort;
import com.pizzeria.internship.order_service.order.OrderQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRankingService {
    private final OrderQueryFacade orderQueryFacade;

    @Transactional(readOnly = true)
    public ProductRankingResponse getProductRanking(
            Long locationId, Instant from, Instant to,
            RankingSort sortBy, Pageable pageable) {
        Page<ProductRankingItem> page =
                orderQueryFacade.getProductRanking(locationId, from, to, sortBy, pageable);
        BigDecimal totalRevenue = orderQueryFacade.getTotalRevenue(locationId, from, to);

        List<ProductRankingItem> items = page.getContent().stream()
                .map(item -> new ProductRankingItem(
                        item.productId(),
                        item.historicalName(),
                        item.totalQuantity(),
                        item.totalRevenue(),
                        calculatePercentage(item.totalRevenue(), totalRevenue)
                ))
                .collect(Collectors.toList());

        return new ProductRankingResponse(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    private double calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
