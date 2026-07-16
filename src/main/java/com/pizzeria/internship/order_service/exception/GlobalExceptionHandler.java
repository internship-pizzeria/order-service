package com.pizzeria.internship.order_service.exception;

import com.pizzeria.internship.order_service.order.InvalidOrderException;
import com.pizzeria.internship.order_service.product.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", ZonedDateTime.now(ZoneOffset.UTC),
                "status", 404,
                "error", "Not Found",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(InvalidOrderException.class)
    ResponseEntity<Map<String, Object>> handleInvalidOrder(InvalidOrderException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", ZonedDateTime.now(ZoneOffset.UTC),
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(Exception e) {
        log.error("Unexpected exception", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
