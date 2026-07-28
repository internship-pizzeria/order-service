package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import com.pizzeria.internship.order_service.admin.RankingSort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<DailyAggregation> getDailyAggregations(Instant from, Instant to) {
        return orderRepository.getDailyAggregations(from, to);
    }

    public Page<ProductRankingItem> getProductRanking(
            Long locationId, Instant from, Instant to,
            RankingSort sortBy, Pageable pageable) {

        Sort sort = switch (sortBy) {
            case BY_QUANTITY -> Sort.by(Sort.Direction.DESC, "totalQuantity");
            case BY_REVENUE -> Sort.by(Sort.Direction.DESC, "totalRevenue");
        };

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort);

        return orderItemRepository.getProductRanking(locationId, from, to, pageRequest);
    }

    public BigDecimal getTotalRevenue(Long locationId, Instant from, Instant to) {
        return orderItemRepository.getTotalRevenue(locationId, from, to);
    }
}
