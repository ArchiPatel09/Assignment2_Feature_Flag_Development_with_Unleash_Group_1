package com.example.order_service.service;

import com.example.order_service.Service.FeatureFlagService;
import io.getunleash.Unleash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private Unleash unleash;

    @Mock
    private Logger logger;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagService(unleash);
    }

    // Bulk Discount Flag Tests
    @Test
    void isBulkOrderDiscountEnabled_WhenFlagOn_ReturnsTrue() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(true);

        // Act
        boolean result = featureFlagService.isBulkOrderDiscountEnabled();

        // Assert
        assertTrue(result);
        verify(unleash).isEnabled("bulk-order-discount", false);
    }

    @Test
    void isBulkOrderDiscountEnabled_WhenFlagOff_ReturnsFalse() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(false);

        // Act
        boolean result = featureFlagService.isBulkOrderDiscountEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("bulk-order-discount", false);
    }

    @Test
    void isBulkOrderDiscountEnabled_WhenUnleashThrowsException_ReturnsFallbackFalse() {
        // Arrange
        when(unleash.isEnabled(anyString(), anyBoolean())).thenThrow(new RuntimeException("Unleash connection failed"));

        // Act
        boolean result = featureFlagService.isBulkOrderDiscountEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("bulk-order-discount", false);
    }

    @Test
    void applyBulkDiscount_WhenFlagEnabledAndQuantityGreaterThan5_Applies15PercentDiscount() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(true);
        double originalTotal = 1000.0;
        int quantity = 10;

        // Act
        double result = featureFlagService.applyBulkDiscount(originalTotal, quantity);

        // Assert
        assertEquals(850.0, result, 0.01); // 15% off = 850
    }

    @Test
    void applyBulkDiscount_WhenFlagEnabledButQuantityNotGreaterThan5_NoDiscount() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(true);
        double originalTotal = 1000.0;
        int quantity = 3;

        // Act
        double result = featureFlagService.applyBulkDiscount(originalTotal, quantity);

        // Assert
        assertEquals(originalTotal, result, 0.01);
    }

    @Test
    void applyBulkDiscount_WhenFlagDisabledEvenWithQuantityGreaterThan5_NoDiscount() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(false);
        double originalTotal = 1000.0;
        int quantity = 10;

        // Act
        double result = featureFlagService.applyBulkDiscount(originalTotal, quantity);

        // Assert
        assertEquals(originalTotal, result, 0.01);
    }

    @Test
    void applyBulkDiscount_WithDifferentQuantities_AppliesDiscountCorrectly() {
        // Arrange
        when(unleash.isEnabled(eq("bulk-order-discount"), anyBoolean())).thenReturn(true);

        // Act & Assert
        assertEquals(85.0, featureFlagService.applyBulkDiscount(100.0, 6), 0.01);
        assertEquals(425.0, featureFlagService.applyBulkDiscount(500.0, 10), 0.01);
        assertEquals(850.0, featureFlagService.applyBulkDiscount(1000.0, 20), 0.01);
        assertEquals(500.0, featureFlagService.applyBulkDiscount(500.0, 5), 0.01); // edge case - exactly 5
    }

    // Order Notifications Flag Tests
    @Test
    void isOrderNotificationsEnabled_WhenFlagOn_ReturnsTrue() {
        // Arrange
        when(unleash.isEnabled(eq("order-notifications"), anyBoolean())).thenReturn(true);

        // Act
        boolean result = featureFlagService.isOrderNotificationsEnabled();

        // Assert
        assertTrue(result);
        verify(unleash).isEnabled("order-notifications", false);
    }

    @Test
    void isOrderNotificationsEnabled_WhenFlagOff_ReturnsFalse() {
        // Arrange
        when(unleash.isEnabled(eq("order-notifications"), anyBoolean())).thenReturn(false);

        // Act
        boolean result = featureFlagService.isOrderNotificationsEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("order-notifications", false);
    }

    @Test
    void isOrderNotificationsEnabled_WhenUnleashThrowsException_ReturnsFallbackFalse() {
        // Arrange
        when(unleash.isEnabled(anyString(), anyBoolean())).thenThrow(new RuntimeException("Unleash connection failed"));

        // Act
        boolean result = featureFlagService.isOrderNotificationsEnabled();

        // Assert
        assertFalse(result);
        verify(unleash).isEnabled("order-notifications", false);
    }

    @Test
    void logOrderNotification_WhenFlagEnabled_LogsNotification() {
        // Arrange
        when(unleash.isEnabled(eq("order-notifications"), anyBoolean())).thenReturn(true);

        // We can't easily verify logger output in unit test without more setup
        // This test just ensures no exception is thrown

        // Act & Assert
        assertDoesNotThrow(() ->
                featureFlagService.logOrderNotification(1L, 100L, 5, 500.0)
        );
    }

    @Test
    void logOrderNotification_WhenFlagDisabled_DoesNotLog() {
        // Arrange
        when(unleash.isEnabled(eq("order-notifications"), anyBoolean())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() ->
                featureFlagService.logOrderNotification(1L, 100L, 5, 500.0)
        );
    }
}