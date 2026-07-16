package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.product.Product;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.product.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private Product margherita;
    private Product pepperoni;

    private final BigDecimal TEST_MARGHERITA_PRICE = BigDecimal.valueOf(29.99);
    private final BigDecimal TEST_PEPPERONI_PRICE = BigDecimal.valueOf(34.99);
    private final String TEST_PHONE_NUMBER = "+48 123 456 789";
    private final String TEST_ADDRESS = "address";
    private final String TEST_NAME = "name";
    private final Long TEST_LOCATION_ID = 10L;

    @BeforeEach
    void setUp() {
        margherita = Product.fromDto(new ProductDto(1L, "Margherita", "Classic pizza", TEST_MARGHERITA_PRICE, TEST_LOCATION_ID));
        pepperoni = Product.fromDto(new ProductDto(2L, "Pepperoni", "Spicy pizza", TEST_PEPPERONI_PRICE, TEST_LOCATION_ID));
    }

    @Test
    void createOrder_shouldSetOrderFields() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(TEST_NAME, result.getCustomerName());
        assertEquals(TEST_PHONE_NUMBER, result.getPhoneNumber());
        assertEquals(TEST_ADDRESS, result.getDeliveryAddress());
        assertEquals(TEST_LOCATION_ID, result.getLocationId());
        assertEquals(TEST_MARGHERITA_PRICE, result.getTotalPrice());
    }

    @Test
    void createOrder_shouldCreateOneItemForSingleProduct() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().getFirst().getProductId());
        assertEquals(1, result.getItems().getFirst().getQuantity());
    }

    @Test
    void createOrder_shouldCreateMultipleItemsForDifferentProducts() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 1), item(2L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L), eq(TEST_LOCATION_ID))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("64.98"), result.getTotalPrice());
    }

    @Test
    void createOrder_shouldUseQuantityFromRequest() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 4), item(2L, 2));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L), eq(TEST_LOCATION_ID))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(2, result.getItems().size());

        OrderItem margheritaItem = result.getItems().stream()
                .filter(i -> i.getProductId().equals(1L))
                .findFirst().orElseThrow();
        assertEquals(4, margheritaItem.getQuantity());

        OrderItem pepperoniItem = result.getItems().stream()
                .filter(i -> i.getProductId().equals(2L))
                .findFirst().orElseThrow();
        assertEquals(2, pepperoniItem.getQuantity());

        assertEquals(new BigDecimal("189.94"), result.getTotalPrice());
    }

    @Test
    void createOrder_shouldCalculateTotalPriceAutomatically() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 2), item(2L, 2));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L), eq(TEST_LOCATION_ID))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(new BigDecimal("129.96"), result.getTotalPrice());
    }

    @Test
    void createOrder_shouldSaveHistoricalData() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        OrderItem orderItem = result.getItems().getFirst();
        assertEquals("Margherita", orderItem.getHistoricalName());
        assertEquals(new BigDecimal("29.99"), orderItem.getHistoricalPrice());
    }

    @Test
    void createOrder_shouldCallProductClientPerItem() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 2), item(2L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L), eq(TEST_LOCATION_ID))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        orderService.createOrder(request);

        // Then
        verify(productClient, times(1)).getProductById(1L, TEST_LOCATION_ID);
        verify(productClient, times(1)).getProductById(2L, TEST_LOCATION_ID);
    }

    @Test
    void createOrder_shouldCallRepositorySave() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        orderService.createOrder(request);

        // Then
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_shouldRejectPhoneNumberWithLetters() {
        // Given
        OrderRequestDto request = buildRequestWithPhone("+48 AAA XXX RRR", item(1L, 1));

        // When & Then
        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(request));
        assertTrue(exception.getMessage().contains("Invalid phone number"));
    }

    @Test
    void createOrder_shouldAcceptValidPhoneNumberFormats() {
        // Given
        OrderRequestDto request = buildRequestWithPhone("+48 123 456 789", item(1L, 1));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals("+48 123 456 789", result.getPhoneNumber());
    }

    @Test
    void createOrder_shouldRejectQuantityExceedingLimit() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 51));

        // When & Then
        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(request));
        assertTrue(exception.getMessage().contains("Total quantity exceeds limit"));
    }

    @Test
    void createOrder_shouldAcceptQuantityAtLimit() {
        // Given
        OrderRequestDto request = buildRequest(item(1L, 50));
        when(productClient.getProductById(eq(1L), eq(TEST_LOCATION_ID))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertEquals(50, result.getItems().getFirst().getQuantity());
    }

    private OrderItemRequestDto item(Long productId, int quantity) {
        return new OrderItemRequestDto(productId, quantity);
    }

    private OrderRequestDto buildRequest(OrderItemRequestDto... items) {
        return new OrderRequestDto(TEST_NAME, TEST_PHONE_NUMBER, TEST_ADDRESS, TEST_LOCATION_ID, List.of(items));
    }

    private OrderRequestDto buildRequestWithPhone(String phone, OrderItemRequestDto... items) {
        return new OrderRequestDto(TEST_NAME, phone, TEST_ADDRESS, TEST_LOCATION_ID, List.of(items));
    }
}
