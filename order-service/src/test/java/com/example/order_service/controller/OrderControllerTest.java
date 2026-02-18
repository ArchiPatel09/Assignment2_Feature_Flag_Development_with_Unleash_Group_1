package com.example.order_service.controller;

import com.example.order_service.Controller.OrderController;
import com.example.order_service.Feign.Dto.OrderRequest;
import com.example.order_service.Model.Order;
import com.example.order_service.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private List<Order> testOrders;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();

        Order order1 = new Order(1L, 5, 500.0, "PENDING");
        order1.setId(1L);

        Order order2 = new Order(2L, 3, 300.0, "COMPLETED");
        order2.setId(2L);

        Order order3 = new Order(1L, 10, 850.0, "PENDING");
        order3.setId(3L);

        testOrders = Arrays.asList(order1, order2, order3);

        orderRequest = new OrderRequest();
        orderRequest.setProductId(1L);
        orderRequest.setQuantity(10);
        orderRequest.setStatus("PENDING");
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(testOrders);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].quantity").value(5))
                .andExpect(jsonPath("$[0].totalPrice").value(500.0))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].productId").value(1))
                .andExpect(jsonPath("$[2].quantity").value(10))
                .andExpect(jsonPath("$[2].totalPrice").value(850.0));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() throws Exception {
        // Arrange
        Long orderId = 1L;
        Order order = new Order(1L, 5, 500.0, "PENDING");
        order.setId(orderId);
        when(orderService.getOrderById(orderId)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.totalPrice").value(500.0))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnNull() throws Exception {
        // Arrange
        Long orderId = 999L;
        when(orderService.getOrderById(orderId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    void createOrder_WithValidRequest_ShouldCreateAndReturnOrder() throws Exception {
        // Arrange
        Order createdOrder = new Order(1L, 10, 850.0, "PENDING");
        createdOrder.setId(4L);
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.totalPrice").value(850.0))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void createOrder_WithBulkOrder_ShouldReturnDiscountedPrice() throws Exception {
        // Arrange
        orderRequest.setQuantity(10);
        Order createdOrder = new Order(1L, 10, 850.0, "PENDING");
        createdOrder.setId(5L);
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.totalPrice").value(850.0));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void createOrder_WithRegularOrder_ShouldReturnRegularPrice() throws Exception {
        // Arrange
        orderRequest.setQuantity(3);
        Order createdOrder = new Order(1L, 3, 300.0, "PENDING");
        createdOrder.setId(6L);
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(300.0));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void createOrder_WithInvalidProductId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product not found"));  // Verify error message

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }
}