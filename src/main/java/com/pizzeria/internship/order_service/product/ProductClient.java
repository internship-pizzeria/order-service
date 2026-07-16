package com.pizzeria.internship.order_service.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
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

    public ProductDto getProductById(Long productId, Long locationId) {
        int attempt = 0;
        while (true) {
            try {
                return restClient.get()
                        .uri("/api/v1/products/{id}?locationId={locationId}", productId, locationId)
                        .retrieve()
                        .body(ProductDto.class);
            } catch (HttpClientErrorException.NotFound e) {
                throw new ProductNotFoundException(productId);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw e;
                }
                log.warn("Attempt {}/{} failed for product {}, retrying in {}ms",
                        attempt, MAX_RETRIES, productId, RETRY_DELAY_MS, e);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }
}
