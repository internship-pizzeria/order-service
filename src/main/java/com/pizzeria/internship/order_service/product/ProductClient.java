package com.pizzeria.internship.order_service.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final RestClient restClient;

    public ProductClient(@Value("${catalog-service.url}") String baseUrl) {
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
    public Product getProductById(Long productId, Long locationId) {
        try {
            ProductDto dto = restClient.get()
                    .uri("/api/v1/products/{id}?locationId={locationId}", productId, locationId)
                    .retrieve()
                    .body(ProductDto.class);
            return Product.fromDto(dto);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        }
    }

    @Recover
    Product recover(HttpServerErrorException e, Long productId, Long locationId) {
        log.error("Failed to fetch product {} after retries", productId, e);
        throw e;
    }

    @Recover
    Product recover(ResourceAccessException e, Long productId, Long locationId) {
        log.error("Failed to fetch product {} after retries", productId, e);
        throw e;
    }
}
