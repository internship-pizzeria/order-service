package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<DailyAggregation> getDailyAggregations(Instant from, Instant to) {
        return orderRepository.getDailyAggregations(from, to);
    }

    public Page<ProductRankingItem> getProductRanking(
            Long locationId, Instant from, Instant to,
            Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize());

        return orderItemRepository.getProductRanking(locationId, from, to, pageRequest);
    }

    public BigDecimal getTotalRevenue(Long locationId, Instant from, Instant to) {
        return orderItemRepository.getTotalRevenue(locationId, from, to);
    }
}
