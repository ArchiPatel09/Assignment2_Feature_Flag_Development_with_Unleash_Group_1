package com.example.product_service.controller;

import com.example.product_service.Model.Product;
import com.example.product_service.Service.FeatureFlagService;
import com.example.product_service.Service.ProductService;
import com.example.product_service.controller.ProductController;
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
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private FeatureFlagService featureFlagService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();

        // Create test products with IDs
        Product laptop = new Product("Laptop", 1000.0, 10);
        laptop.setId(1L);

        Product mouse = new Product("Mouse", 50.0, 100);
        mouse.setId(2L);

        Product keyboard = new Product("Keyboard", 80.0, 50);
        keyboard.setId(3L);

        testProducts = Arrays.asList(laptop, mouse, keyboard);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(testProducts);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(1000.0))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[1].price").value(50.0))
                .andExpect(jsonPath("$[2].name").value("Keyboard"))
                .andExpect(jsonPath("$[2].price").value(80.0));

        verify(productService, times(1)).getAllProducts();
        verifyNoInteractions(featureFlagService);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        // Arrange
        Long productId = 1L;
        when(productService.getProductById(productId)).thenReturn(testProducts.get(0));

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1000.0));

        verify(productService, times(1)).getProductById(productId);
        verifyNoInteractions(featureFlagService);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldReturnNull() throws Exception {
        // Arrange
        Long productId = 999L;
        when(productService.getProductById(productId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(productService, times(1)).getProductById(productId);
        verifyNoInteractions(featureFlagService);
    }

    @Test
    void getPremiumProducts_WhenFlagEnabled_ShouldReturnDiscountedPrices() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(testProducts);

        // Mock the applyPremiumPricing method directly since that's what controller calls
        when(featureFlagService.applyPremiumPricing(1000.0)).thenReturn(900.0);
        when(featureFlagService.applyPremiumPricing(50.0)).thenReturn(45.0);
        when(featureFlagService.applyPremiumPricing(80.0)).thenReturn(72.0);

        // Act & Assert
        mockMvc.perform(get("/api/products/premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(900.0))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[1].price").value(45.0))
                .andExpect(jsonPath("$[2].name").value("Keyboard"))
                .andExpect(jsonPath("$[2].price").value(72.0));

        verify(productService, times(1)).getAllProducts();
        verify(featureFlagService, times(3)).applyPremiumPricing(anyDouble());
        // Don't verify isPremiumPricingEnabled() since controller doesn't call it directly
    }

    @Test
    void getPremiumProducts_WhenFlagDisabled_ShouldReturnRegularPrices() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(testProducts);

        // When flag is disabled, applyPremiumPricing returns original price
        when(featureFlagService.applyPremiumPricing(1000.0)).thenReturn(1000.0);
        when(featureFlagService.applyPremiumPricing(50.0)).thenReturn(50.0);
        when(featureFlagService.applyPremiumPricing(80.0)).thenReturn(80.0);

        // Act & Assert
        mockMvc.perform(get("/api/products/premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].price").value(1000.0))
                .andExpect(jsonPath("$[1].price").value(50.0))
                .andExpect(jsonPath("$[2].price").value(80.0));

        verify(productService, times(1)).getAllProducts();
        verify(featureFlagService, times(3)).applyPremiumPricing(anyDouble());
        // Don't verify isPremiumPricingEnabled() since controller doesn't call it directly
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() throws Exception {
        // Arrange
        Product newProduct = new Product("Monitor", 300.0, 20);
        Product savedProduct = new Product("Monitor", 300.0, 20);
        savedProduct.setId(4L);

        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Monitor"))
                .andExpect(jsonPath("$.price").value(300.0))
                .andExpect(jsonPath("$.quantity").value(20));

        verify(productService, times(1)).createProduct(any(Product.class));
        verifyNoInteractions(featureFlagService);
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() throws Exception {
        // Arrange
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk());

        verify(productService, times(1)).deleteProduct(productId);
        verifyNoInteractions(featureFlagService);
    }
}