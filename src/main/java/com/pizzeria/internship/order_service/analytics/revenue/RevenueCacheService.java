package com.pizzeria.internship.order_service.analytics.revenue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RevenueCacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String REVENUE_PREFIX = "revenue:";
    private static final String COUNT_PREFIX = "order_count:";
    private static final Duration TTL = Duration.ofHours(24);

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC);

    public void addOrderRevenue(Long locationId, BigDecimal revenue) {
        String today = dateFormatter.format(LocalDate.now(ZoneOffset.UTC));
        String revenueKey = REVENUE_PREFIX + today + ":" + locationId;
        String countKey = COUNT_PREFIX + today + ":" + locationId;

        redisTemplate.opsForValue().increment(revenueKey, revenue.doubleValue());
        redisTemplate.expire(revenueKey, TTL);

        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, TTL);
    }

    public BigDecimal getTodayRevenue(Long locationId) {
        String today = dateFormatter.format(LocalDate.now(ZoneOffset.UTC));
        String key = REVENUE_PREFIX + today + ":" + locationId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
    }

    public long getTodayOrderCount(Long locationId) {
        String today = dateFormatter.format(LocalDate.now(ZoneOffset.UTC));
        String key = COUNT_PREFIX + today + ":" + locationId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    public Map<Long, BigDecimal> getAllLocationsTodayRevenue() {
        String today = dateFormatter.format(LocalDate.now(ZoneOffset.UTC));
        String pattern = REVENUE_PREFIX + today + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> result = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split(":");
            Long locationId = Long.parseLong(parts[2]);
            BigDecimal revenue = new BigDecimal(redisTemplate.opsForValue().get(key));
            result.put(locationId, revenue);
        }
        return result;
    }

    public Map<Long, Long> getAllLocationsTodayOrderCount() {
        String today = dateFormatter.format(LocalDate.now(ZoneOffset.UTC));
        String pattern = COUNT_PREFIX + today + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split(":");
            Long locationId = Long.parseLong(parts[2]);
            long count = Long.parseLong(redisTemplate.opsForValue().get(key));
            result.put(locationId, count);
        }
        return result;
    }
}
