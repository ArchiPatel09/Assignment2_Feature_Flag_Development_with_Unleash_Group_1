package com.example.product_service.Service;

import com.example.product_service.Model.Product;
import com.example.product_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    // repository for product data operations
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // retrieving all products from the database
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // finding product by ID
    public Product getProductById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // saving a new product to the database
    public Product createProduct(Product product) {
        return repository.save(product);
    }

    // deleting product by ID
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }
}
