package com.example.product_service.Repository;

import com.example.product_service.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
// extends JpaRepository to provide CRUD operations for Product entity with Long ID
}
