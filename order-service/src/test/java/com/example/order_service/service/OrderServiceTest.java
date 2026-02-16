package com.example.order_service.service;

import com.example.order_service.Feign.Dto.OrderRequest;
import com.example.order_service.Feign.Dto.ProductDto;
import com.example.order_service.Feign.ProductServiceClient;
import com.example.order_service.Model.Order;
import com.example.order_service.Repository.OrderRepository;
import com.example.order_service.Service.FeatureFlagService;
import com.example.order_service.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private FeatureFlagService featureFlagService;

    @InjectMocks
    private OrderService orderService;

    private ProductDto testProduct;
    private OrderRequest orderRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = new ProductDto();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);
        testProduct.setQuantity(100);

        orderRequest = new OrderRequest();
        orderRequest.setProductId(1L);
        orderRequest.setQuantity(10);
        orderRequest.setStatus("PENDING");

        testOrder = new Order(1L, 10, 1000.0, "PENDING");
        testOrder.setId(1L);
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(
                new Order(1L, 5, 500.0, "PENDING"),
                new Order(2L, 3, 300.0, "COMPLETED")
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnNull() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNull(result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void createOrder_WithBulkDiscountEnabledAndQuantityGreaterThan5_AppliesDiscount() {
        // Arrange
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(featureFlagService.applyBulkDiscount(1000.0, 10)).thenReturn(850.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(850.0, result.getTotalPrice()); // Discount applied
        assertEquals(10, result.getQuantity());
        assertEquals("PENDING", result.getStatus());

        verify(featureFlagService).applyBulkDiscount(1000.0, 10);
        verify(featureFlagService).logOrderNotification(anyLong(), anyLong(), anyInt(), anyDouble());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WithBulkDiscountDisabled_NoDiscountApplied() {
        // Arrange
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(featureFlagService.applyBulkDiscount(1000.0, 10)).thenReturn(1000.0); // No discount
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1000.0, result.getTotalPrice()); // No discount
        assertEquals(10, result.getQuantity());

        verify(featureFlagService).applyBulkDiscount(1000.0, 10);
        verify(featureFlagService).logOrderNotification(anyLong(), anyLong(), anyInt(), anyDouble());
    }

    @Test
    void createOrder_WithSmallQuantity_NoBulkDiscountEvenIfFlagEnabled() {
        // Arrange
        orderRequest.setQuantity(3); // Small quantity
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(featureFlagService.applyBulkDiscount(300.0, 3)).thenReturn(300.0); // No discount for small qty
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(300.0, result.getTotalPrice()); // No discount
        assertEquals(3, result.getQuantity());

        verify(featureFlagService).applyBulkDiscount(300.0, 3);
    }

    @Test
    void createOrder_WhenProductNotFound_ThrowsException() {
        // Arrange
        when(productServiceClient.getProductById(1L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(orderRequest));
        assertEquals("Product not found", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenInsufficientStock_ThrowsException() {
        // Arrange
        testProduct.setQuantity(5); // Only 5 in stock, but order wants 10
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(orderRequest));
        assertEquals("Not enough stock available!", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithZeroQuantity_CalculatesCorrectly() {
        // Arrange
        orderRequest.setQuantity(0);
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(featureFlagService.applyBulkDiscount(0.0, 0)).thenReturn(0.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotalPrice());
        assertEquals(0, result.getQuantity());
    }

    @Test
    void createOrder_VerifiesNotificationIsCalled() {
        // Arrange
        when(productServiceClient.getProductById(1L)).thenReturn(testProduct);
        when(featureFlagService.applyBulkDiscount(anyDouble(), anyInt())).thenReturn(850.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order order = i.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(orderRequest);

        // Assert
        verify(featureFlagService, times(1)).logOrderNotification(
                eq(1L), eq(1L), eq(10), eq(850.0)
        );
    }
}