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

    private final OrderRepository repository;
    private final ProductServiceClient productServiceClient;
    private final FeatureFlagService featureFlagService;

    public OrderService(OrderRepository repository,
                        ProductServiceClient productServiceClient,
                        FeatureFlagService featureFlagService) {
        this.repository = repository;
        this.productServiceClient = productServiceClient;
        this.featureFlagService = featureFlagService;
    }

    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    public Order getOrderById(Long id) {
        return repository.findById(id).orElse(null);
    }

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

        Order order = new Order(
                orderRequest.getProductId(),
                orderRequest.getQuantity(),
                calculatedTotalPrice,
                orderRequest.getStatus()
        );

        Order savedOrder = repository.save(order);

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