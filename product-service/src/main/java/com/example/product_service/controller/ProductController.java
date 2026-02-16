package com.example.product_service.controller;

import com.example.product_service.Model.Product;
import com.example.product_service.Service.ProductService;
import com.example.product_service.Service.FeatureFlagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    private final FeatureFlagService featureFlagService;

    public ProductController(ProductService service, FeatureFlagService featureFlagService) {
        this.service = service;
        this.featureFlagService = featureFlagService;
    }

    // getting all products (unchanged - regular prices)
    @GetMapping
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }

    // getting product by id (unchanged)
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return service.getProductById(id);
    }

    // NEW: premium endpoint with discounted prices
    @GetMapping("/premium")
    public List<Product> getPremiumProducts() {
        List<Product> products = service.getAllProducts();

        // Apply discount if flag is enabled
        return products.stream()
                .map(product -> {
                    Product discountedProduct = new Product(
                            product.getName(),
                            featureFlagService.applyPremiumPricing(product.getPrice()),
                            product.getQuantity()
                    );
                    discountedProduct.setId(product.getId());
                    return discountedProduct;
                })
                .collect(Collectors.toList());
    }

    // posting (adding) product
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return service.createProduct(product);
    }

    // deleting the product
    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id) {
        service.deleteProduct(id);
    }
}