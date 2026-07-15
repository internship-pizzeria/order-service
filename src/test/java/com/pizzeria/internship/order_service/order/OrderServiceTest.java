package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.orderitem.OrderItem;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.product.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private ProductDto margherita;
    private ProductDto pepperoni;

    @BeforeEach
    void setUp() {
        margherita = new ProductDto(1L, "Margherita", new BigDecimal("29.99"));
        pepperoni = new ProductDto(2L, "Pepperoni", new BigDecimal("34.99"));
    }

    @Test
    void createOrder_shouldSetOrderFields() {
        OrderRequestDto request = buildRequest(List.of(1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertEquals("name", result.getCustomer_name());
        assertEquals("123456789", result.getPhone_number());
        assertEquals("address", result.getDelivery_address());
        assertEquals(new BigDecimal("29.99"), result.getTotal_price());
    }

    @Test
    void createOrder_shouldCreateOneItemForSingleProduct() {
        OrderRequestDto request = buildRequest(List.of(1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().getFirst().getProductId());
        assertEquals(1, result.getItems().getFirst().getQuantity());
    }

    @Test
    void createOrder_shouldCreateMultipleItemsForDifferentProducts() {
        OrderRequestDto request = buildRequest(List.of(1L, 2L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(productClient.getProductById(2L)).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("64.98"), result.getTotal_price());
    }

    @Test
    void createOrder_shouldIncrementQuantityForDuplicateProducts() {
        OrderRequestDto request = buildRequest(List.of(1L, 2L, 1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(productClient.getProductById(2L)).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertEquals(2, result.getItems().size());

        OrderItem margheritaItem = result.getItems().stream()
                .filter(item -> item.getProductId().equals(1L))
                .findFirst().orElseThrow();
        assertEquals(2, margheritaItem.getQuantity());

        OrderItem pepperoniItem = result.getItems().stream()
                .filter(item -> item.getProductId().equals(2L))
                .findFirst().orElseThrow();
        assertEquals(1, pepperoniItem.getQuantity());

        assertEquals(new BigDecimal("94.97"), result.getTotal_price());
    }

    @Test
    void createOrder_shouldCalculateTotalPriceAutomatically() {
        OrderRequestDto request = buildRequest(List.of(1L, 2L, 1L, 2L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(productClient.getProductById(2L)).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        assertEquals(new BigDecimal("129.96"), result.getTotal_price());
    }

    @Test
    void createOrder_shouldSaveHistoricalData() {
        OrderRequestDto request = buildRequest(List.of(1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);

        OrderItem item = result.getItems().getFirst();
        assertEquals("Margherita", item.getHistorical_name());
        assertEquals(new BigDecimal("29.99"), item.getHistorical_price());
    }

    @Test
    void createOrder_shouldCallProductClientForDistinctProducts() {
        OrderRequestDto request = buildRequest(List.of(1L, 2L, 1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(productClient.getProductById(2L)).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(request);

        verify(productClient, times(1)).getProductById(1L);
        verify(productClient, times(1)).getProductById(2L);
    }

    @Test
    void createOrder_shouldCallRepositorySave() {
        OrderRequestDto request = buildRequest(List.of(1L));
        when(productClient.getProductById(1L)).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(request);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    private OrderRequestDto buildRequest(List<Long> productIds) {
        return new OrderRequestDto("name", "123456789", "address", productIds);
    }
}
