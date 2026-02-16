package com.example.order_service.Controller;

import com.example.order_service.Feign.Dto.OrderRequest;
import com.example.order_service.Model.Order;
import com.example.order_service.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service){
        this.service = service;
    }

    // fetching all orders from the service
    @GetMapping
    public List<Order> getAllOrders(){
        return service.getAllOrders();
    }

    // retrieving a specific order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id){
        return service.getOrderById(id);
    }

    /*
     creating a new order with the provided request data
     @PostMapping
        public Order createOrder(@RequestBody OrderRequest orderRequest){
            return service.createOrder(orderRequest);
        }
    */

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order order = service.createOrder(orderRequest);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}