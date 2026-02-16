package com.example.order_service.Feign;

import com.example.order_service.Feign.Dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// declaring a feign client for product-service
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductServiceClient {
    // mapping to the GET endpoint of product-service
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable Long id); // retrieves product details by product ID
}
