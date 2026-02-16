package com.example.order_service.Feign.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// rejecting unknown json fields
@JsonIgnoreProperties(ignoreUnknown = false)
public class OrderRequest {
    private Long id;
    private Long productId;
    private int quantity;
    private String status;

    // default constructor
    public OrderRequest() {
    }

    // parameterized constructor
    public OrderRequest(Long id, Long productId, int quantity, String status) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}