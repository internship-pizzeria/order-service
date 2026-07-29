package com.pizzeria.internship.order_service.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LocationClient {

    private static final Logger log = LoggerFactory.getLogger(LocationClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final RestClient restClient;

    public LocationClient(@Value("${catalog-service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Retryable(
            retryFor = {HttpServerErrorException.class, ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public List<LocationResponse> getLocationsByIds(List<Long> ids) {
        String joined = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return restClient.get()
                .uri("/api/v1/locations/batch?ids={ids}", joined)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<Long, String> getCityNameMap(List<Long> ids) {
        return getLocationsByIds(ids).stream()
                .collect(Collectors.toMap(
                        LocationResponse::id,
                        LocationResponse::city
                ));
    }

    @Recover
    List<LocationResponse> recover(HttpServerErrorException e, List<Long> ids) {
        log.error("Failed to fetch locations {} after retries", ids, e);
        throw e;
    }

    @Recover
    List<LocationResponse> recover(ResourceAccessException e, List<Long> ids) {
        log.error("Failed to fetch locations {} after retries", ids, e);
        throw e;
    }

}
