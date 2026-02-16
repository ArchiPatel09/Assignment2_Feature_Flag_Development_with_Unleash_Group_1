package com.example.product_service.service;

import com.example.product_service.Service.FeatureFlagService;
import io.getunleash.Unleash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private Unleash unleash;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagService(unleash);
    }

    @Test
    void isPremiumPricingEnabled_WhenFlagOn_ReturnsTrue() {
        // Arrange
        when(unleash.isEnabled(eq("premium-pricing"), anyBoolean())).thenReturn(true);

        // Act
        boolean result = featureFlagService.isPremiumPricingEnabled();

        // Assert
        assertTrue(result);
        verify(unleash).isEnabled("premium-pricing", false);
    }

    @Test
    void isPremiumPricingEnabled_WhenFlagOff_ReturnsFalse() {
        // Arrange
        when(unleash.isEnabled(eq("premium-pricing"), anyBoolean())).thenReturn(false);

        // Act
        boolean result = featureFlagService.isPremiumPricingEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("premium-pricing", false);
    }

    @Test
    void isPremiumPricingEnabled_WhenUnleashThrowsException_ReturnsFallbackFalse() {
        // Arrange
        when(unleash.isEnabled(anyString(), anyBoolean())).thenThrow(new RuntimeException("Unleash connection failed"));

        // Act
        boolean result = featureFlagService.isPremiumPricingEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("premium-pricing", false);
    }

    @Test
    void applyPremiumPricing_WhenFlagEnabled_Applies10PercentDiscount() {
        // Arrange
        when(unleash.isEnabled(eq("premium-pricing"), anyBoolean())).thenReturn(true);
        double originalPrice = 100.0;

        // Act
        double discountedPrice = featureFlagService.applyPremiumPricing(originalPrice);

        // Assert
        assertEquals(90.0, discountedPrice, 0.01);
    }

    @Test
    void applyPremiumPricing_WhenFlagDisabled_ReturnsOriginalPrice() {
        // Arrange
        when(unleash.isEnabled(eq("premium-pricing"), anyBoolean())).thenReturn(false);
        double originalPrice = 100.0;

        // Act
        double result = featureFlagService.applyPremiumPricing(originalPrice);

        // Assert
        assertEquals(originalPrice, result, 0.01);
    }

    @Test
    void applyPremiumPricing_WhenFlagEnabledForMultiplePrices_AppliesDiscountCorrectly() {
        // Arrange
        when(unleash.isEnabled(eq("premium-pricing"), anyBoolean())).thenReturn(true);

        // Act & Assert
        assertEquals(90.0, featureFlagService.applyPremiumPricing(100.0), 0.01);
        assertEquals(45.0, featureFlagService.applyPremiumPricing(50.0), 0.01);
        assertEquals(135.0, featureFlagService.applyPremiumPricing(150.0), 0.01);
        assertEquals(0.0, featureFlagService.applyPremiumPricing(0.0), 0.01);
    }
}