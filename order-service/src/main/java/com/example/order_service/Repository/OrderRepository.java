package com.example.order_service.Repository;

import com.example.order_service.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // extends JpaRepository to provide CRUD operations for Order entity with Long ID
}
