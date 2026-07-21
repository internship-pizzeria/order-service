package com.pizzeria.internship.order_service.exception;

import com.pizzeria.internship.order_service.order.InvalidOrderException;
import com.pizzeria.internship.order_service.order.InvalidStatusTransitionException;
import com.pizzeria.internship.order_service.order.OrderAccessDeniedException;
import com.pizzeria.internship.order_service.order.OrderNotFoundException;
import com.pizzeria.internship.order_service.product.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleProductNotFound(ProductNotFoundException e) {
        log.warn(e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Not Found");
        problem.setType(URI.create("https://api.pizzeria.com/errors/product-not-found"));
        return problem;
    }

    @ExceptionHandler(InvalidOrderException.class)
    ProblemDetail handleInvalidOrder(InvalidOrderException e) {
        log.warn(e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.pizzeria.com/errors/invalid-order"));
        return problem;
    }

    @ExceptionHandler(HttpServerErrorException.class)
    ProblemDetail handleHttpServerError(HttpServerErrorException e) {
        log.error("Downstream service error: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "Downstream service is unavailable");
        problem.setTitle("Bad Gateway");
        problem.setType(URI.create("https://api.pizzeria.com/errors/downstream-error"));
        return problem;
    }

    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail handleResourceAccess(ResourceAccessException e) {
        log.error("Failed to connect to downstream service", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");
        problem.setTitle("Service Unavailable");
        problem.setType(URI.create("https://api.pizzeria.com/errors/service-unavailable"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.pizzeria.com/errors/validation"));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResourceFound(NoResourceFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "The requested resource was not found");
        problem.setTitle("Not Found");
        return problem;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED, "Method " + e.getMethod() + " is not supported");
        problem.setTitle("Method Not Allowed");
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFound(OrderNotFoundException e) {
        log.warn(e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Not Found");
        problem.setType(URI.create("https://api.pizzeria.com/errors/order-not-found"));
        return problem;
    }

    @ExceptionHandler(OrderAccessDeniedException.class)
    ProblemDetail handleOrderAccessDenied(OrderAccessDeniedException e) {
        log.warn(e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, e.getMessage());
        problem.setTitle("Forbidden");
        problem.setType(URI.create("https://api.pizzeria.com/errors/access-denied"));
        return problem;
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    ProblemDetail handleInvalidStatusTransition(InvalidStatusTransitionException e) {
        log.warn(e.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.pizzeria.com/errors/invalid-status-transition"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(Exception e) {
        log.error("Unexpected exception", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.pizzeria.com/errors/internal-error"));
        return problem;
    }
}
