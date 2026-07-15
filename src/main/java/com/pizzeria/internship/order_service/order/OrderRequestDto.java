package com.pizzeria.internship.order_service.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequestDto(String customerName, String phoneNumber, String deliveryAddress, List<Long> productIds) {

}
