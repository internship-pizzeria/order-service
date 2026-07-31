package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle management: creation, listing for the current location, status tracking and status updates with validated transitions.")
class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new order",
            description = "Creates a new pizza order. The products are resolved and validated against the catalog-service " +
                    "for the given location, prices are snapshotted into the order items and the total is calculated. " +
                    "A new order is always created with status NEW. Returns the persisted order with all historical " +
                    "product details. This endpoint is public (no auth headers required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order (bad phone number, empty items, quantity out of range 1-50, unavailable product)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "One of the requested products does not exist in the catalog",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Catalog service is unavailable",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    OrderResponseDto createOrder(@RequestBody OrderRequestDto orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping
    @Operation(
            summary = "List orders for the current location",
            description = "Returns all orders created for the location taken from the 'LocationId' header. " +
                    "When the 'status' query parameter is provided, only orders in that status are returned.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the orders are scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders for the current location"),
            @ApiResponse(responseCode = "400", description = "Invalid status filter value",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    List<OrderResponseDto> getOrdersByLocation(
            @Parameter(description = "Optional filter by order status. Case-insensitive.", schema = @Schema(implementation = Status.class, example = "NEW"))
            @RequestParam(required = false) String status) {
        Status statusFilter;
        if (status != null) {
            try {
                statusFilter = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidOrderException("Invalid status value: " + status);
            }
        } else {
            statusFilter = null;
        }
        return orderService.getOrdersByLocation(statusFilter);
    }

    @GetMapping("/{orderId}/status")
    @Operation(
            summary = "Get order status",
            description = "Returns the current state of an order identified by its UUID, including total price and " +
                    "all line items. This endpoint is public (no auth headers required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order with the given ID does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    OrderResponseDto getOrderStatus(
            @Parameter(description = "Unique identifier of the order", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID orderId) {
        return orderService.getOrderStatusById(orderId);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(
            summary = "Update order status",
            description = "Moves an order to a new status. The transition is validated against the allowed state machine " +
                    "(e.g. NEW -> ACCEPTED, READY -> PAID). The order must belong to the location from the 'LocationId' header. " +
                    "Optimistic locking failures are retried automatically.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the order must belong to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value or transition not allowed from the current status",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Order belongs to a different location",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order with the given ID does not exist",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    OrderResponseDto updateOrderStatus(
            @Parameter(description = "Unique identifier of the order", required = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID orderId,
            @RequestBody UpdateStatusRequestDto request) {
        return orderService.updateOrderStatus(orderId, request);
    }
}
