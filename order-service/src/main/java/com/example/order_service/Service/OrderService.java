package com.example.order_service.Service;

import com.example.order_service.Feign.Dto.OrderRequest;
import com.example.order_service.Feign.Dto.ProductDto;
import com.example.order_service.Feign.ProductServiceClient;
import com.example.order_service.Model.Order;
import com.example.order_service.Repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    // repository for database operations
    private final OrderRepository repository;
    // feign client for communicating with product service
    private final ProductServiceClient productServiceClient;
    // feature  flag service for conditional business logic
    private final FeatureFlagService featureFlagService;

    // constructor
    public OrderService(OrderRepository repository,
                        ProductServiceClient productServiceClient,
                        FeatureFlagService featureFlagService) {
        this.repository = repository;
        this.productServiceClient = productServiceClient;
        this.featureFlagService = featureFlagService;
    }

    // retrieving all orders
    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    // retrieving a single order by id
    public Order getOrderById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // creating a new order
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        ProductDto product = productServiceClient.getProductById(orderRequest.getProductId());

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        if (product.getQuantity() < orderRequest.getQuantity()) {
            throw new RuntimeException("Not enough stock available!");
        }

        double baseTotalPrice = product.getPrice() * orderRequest.getQuantity();

        double calculatedTotalPrice = featureFlagService.applyBulkDiscount(
                baseTotalPrice,
                orderRequest.getQuantity()
        );

        // creating order entity
        Order order = new Order(
                orderRequest.getProductId(),
                orderRequest.getQuantity(),
                calculatedTotalPrice,
                orderRequest.getStatus()
        );

        Order savedOrder = repository.save(order);

        // conditionally log notification
        featureFlagService.logOrderNotification(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getTotalPrice()
        );

        logger.info("Order Notification: Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }
}