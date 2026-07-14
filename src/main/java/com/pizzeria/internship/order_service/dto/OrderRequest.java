package com.pizzeria.internship.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private String phoneNumber;
    private String deliveryAddress;
    private List<Long> productIds;
    private BigDecimal totalPrice;
}
