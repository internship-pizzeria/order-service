package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.product.Product;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.product.ProductDto;
import com.pizzeria.internship.order_service.user.UserIdAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        margherita = Product.fromDto(new ProductDto(1L, "Margherita", "Classic pizza", TEST_MARGHERITA_PRICE));
        pepperoni = Product.fromDto(new ProductDto(2L, "Pepperoni", "Spicy pizza", TEST_PEPPERONI_PRICE));
        UserIdAuthenticationToken token = new UserIdAuthenticationToken(TEST_USER_ID, TEST_LOCATION_ID);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_shouldSetOrderFields() {
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(TEST_ADDRESS, result.deliveryAddress());
        assertEquals(TEST_MARGHERITA_PRICE, result.totalPrice());
        assertEquals("NEW", result.status());
    }

    @Test
    void createOrder_shouldCreateOneItemForSingleProduct() {
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(1, result.items().size());
        assertEquals(1L, result.items().getFirst().productId());
        assertEquals(1, result.items().getFirst().quantity());
    }

    @Test
    void createOrder_shouldCreateMultipleItemsForDifferentProducts() {
        OrderRequestDto request = buildRequest(item(1L, 1), item(2L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(2, result.items().size());
        assertEquals(new BigDecimal("64.98"), result.totalPrice());
    }

    @Test
    void createOrder_shouldUseQuantityFromRequest() {
        OrderRequestDto request = buildRequest(item(1L, 4), item(2L, 2));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(2, result.items().size());

        OrderItemResponseDto margheritaItem = result.items().stream()
                .filter(i -> i.productId().equals(1L))
                .findFirst().orElseThrow();
        assertEquals(4, margheritaItem.quantity());

        OrderItemResponseDto pepperoniItem = result.items().stream()
                .filter(i -> i.productId().equals(2L))
                .findFirst().orElseThrow();
        assertEquals(2, pepperoniItem.quantity());

        assertEquals(new BigDecimal("189.94"), result.totalPrice());
    }

    @Test
    void createOrder_shouldCalculateTotalPriceAutomatically() {
        OrderRequestDto request = buildRequest(item(1L, 2), item(2L, 2));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(new BigDecimal("129.96"), result.totalPrice());
    }

    @Test
    void createOrder_shouldSaveHistoricalData() {
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        OrderItemResponseDto orderItem = result.items().getFirst();
        assertEquals("Margherita", orderItem.historicalName());
        assertEquals(new BigDecimal("29.99"), orderItem.historicalPrice());
    }

    @Test
    void createOrder_shouldCallProductClientPerItem() {
        OrderRequestDto request = buildRequest(item(1L, 2), item(2L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(productClient.getProductById(eq(2L))).thenReturn(pepperoni);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(request);

        verify(productClient, times(1)).getProductById(1L);
        verify(productClient, times(1)).getProductById(2L);
    }

    @Test
    void createOrder_shouldCallRepositorySave() {
        OrderRequestDto request = buildRequest(item(1L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(request);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_shouldRejectPhoneNumberWithLetters() {
        OrderRequestDto request = buildRequestWithPhone("+48 AAA XXX RRR", item(1L, 1));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(request));
        assertTrue(exception.getMessage().contains("Invalid phone number"));
    }

    @Test
    void createOrder_shouldAcceptValidPhoneNumberFormats() {
        OrderRequestDto request = buildRequestWithPhone(TEST_PHONE_NUMBER, item(1L, 1));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals("NEW", result.status());
    }

    @Test
    void createOrder_shouldRejectQuantityExceedingLimit() {
        OrderRequestDto request = buildRequest(item(1L, 51));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.createOrder(request));
        assertTrue(exception.getMessage().contains("Total quantity exceeds limit"));
    }

    @Test
    void createOrder_shouldAcceptQuantityAtLimit() {
        OrderRequestDto request = buildRequest(item(1L, 50));
        when(productClient.getProductById(eq(1L))).thenReturn(margherita);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto result = orderService.createOrder(request);

        assertEquals(50, result.items().getFirst().quantity());
    }

    // --- Status transition tests ---

    @Test
    void updateStatus_shouldTransitionNewToAccepted() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("ACCEPTED");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("ACCEPTED", result.status());
        verify(orderRepository).updateStatus(orderId, Status.ACCEPTED);
    }

    @Test
    void updateStatus_shouldTransitionAcceptedToInProgress() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.ACCEPTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("IN_PROGRESS");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("IN_PROGRESS", result.status());
    }

    @Test
    void updateStatus_shouldTransitionInProgressToReady() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.IN_PROGRESS);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("READY");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("READY", result.status());
    }

    @Test
    void updateStatus_shouldTransitionReadyToPaid() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.READY);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("PAID");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("PAID", result.status());
    }

    @Test
    void updateStatus_shouldTransitionReadyToInDelivery() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.READY);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("IN_DELIVERY");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("IN_DELIVERY", result.status());
    }

    @Test
    void updateStatus_shouldTransitionInDeliveryToDelivered() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.IN_DELIVERY);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("DELIVERED");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("DELIVERED", result.status());
    }

    @Test
    void updateStatus_shouldRejectNewToInProgress() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("IN_PROGRESS");
        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, request));
    }

    @Test
    void updateStatus_shouldRejectNewToReady() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("READY");
        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, request));
    }

    @Test
    void updateStatus_shouldRejectPaidToAnyStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, new UpdateStatusRequestDto("IN_PROGRESS")));
    }

    @Test
    void updateStatus_shouldRejectFromAnyStatusToNew() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.ACCEPTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, new UpdateStatusRequestDto("NEW")));
    }

    @Test
    void updateStatus_shouldRejectWhenLocationMismatch() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW, 999L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("ACCEPTED");
        assertThrows(OrderAccessDeniedException.class,
                () -> orderService.updateOrderStatus(orderId, request));
    }

    @Test
    void updateStatus_shouldReturn404ForNonexistentOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("ACCEPTED");
        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, request));
    }

    @Test
    void updateStatus_shouldAllowRejectionFromNew() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("REJECTED");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("REJECTED", result.status());
        verify(orderRepository).updateStatus(orderId, Status.REJECTED);
    }

    @Test
    void updateStatus_shouldAllowRejectionFromAccepted() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.ACCEPTED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("REJECTED");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("REJECTED", result.status());
    }

    @Test
    void updateStatus_shouldAllowRejectionFromInProgress() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.IN_PROGRESS);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("REJECTED");
        OrderResponseDto result = orderService.updateOrderStatus(orderId, request);

        assertEquals("REJECTED", result.status());
    }

    @Test
    void updateStatus_shouldThrowOnInvalidStatusValue() {
        UUID orderId = UUID.randomUUID();
        Order order = buildOrder(orderId, Status.NEW);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto("BOGUS");
        InvalidOrderException ex = assertThrows(InvalidOrderException.class,
                () -> orderService.updateOrderStatus(orderId, request));
        assertTrue(ex.getMessage().contains("Invalid status value"));
    }

    // --- getOrdersByLocation with status filter ---

    @Test
    void getOrdersByLocation_shouldReturnAllOrders() {
        Order order1 = buildOrder(UUID.randomUUID(), Status.NEW);
        Order order2 = buildOrder(UUID.randomUUID(), Status.ACCEPTED);
        when(orderRepository.findByLocationId(TEST_LOCATION_ID)).thenReturn(List.of(order1, order2));

        List<OrderResponseDto> result = orderService.getOrdersByLocation();

        assertEquals(2, result.size());
        verify(orderRepository).findByLocationId(TEST_LOCATION_ID);
        verify(orderRepository, never()).findByLocationIdAndStatusIn(any(), any());
    }

    @Test
    void getOrdersByLocation_shouldFilterByStatus() {
        Order order = buildOrder(UUID.randomUUID(), Status.NEW);
        when(orderRepository.findByLocationIdAndStatusIn(TEST_LOCATION_ID, List.of(Status.NEW)))
                .thenReturn(List.of(order));

        List<OrderResponseDto> result = orderService.getOrdersByLocation(Status.NEW);

        assertEquals(1, result.size());
        assertEquals("NEW", result.getFirst().status());
        verify(orderRepository).findByLocationIdAndStatusIn(TEST_LOCATION_ID, List.of(Status.NEW));
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

    private Order buildOrder(UUID id, Status status) {
        return buildOrder(id, status, TEST_LOCATION_ID);
    }

    private Order buildOrder(UUID id, Status status, Long locationId) {
        return Order.builder()
                .id(id)
                .customerName(TEST_NAME)
                .phoneNumber("48123456789")
                .deliveryAddress(TEST_ADDRESS)
                .locationId(locationId)
                .status(status)
                .totalPrice(BigDecimal.TEN)
                .build();
    }
}
