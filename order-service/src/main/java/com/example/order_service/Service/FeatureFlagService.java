package com.example.order_service.Service;

import io.getunleash.Unleash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    // logger for tracking feature flag
    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);
    // injected unleash client
    private final Unleash unleash;

    // constructor
    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    // checking whether the 'order-notifications' feature flag is enabled
    public boolean isOrderNotificationsEnabled() {
        try {
            return unleash.isEnabled("order-notifications", false);
        } catch (Exception e) {
            logger.error("Error checking order-notifications flag: {}", e.getMessage());
            return false;
        }
    }

    // checking whether the 'bulk-order-discount' feature flag is enabled
    public boolean isBulkOrderDiscountEnabled() {
        try {
            return unleash.isEnabled("bulk-order-discount", false);
        } catch (Exception e) {
            logger.error("Error checking bulk-order-discount flag: {}", e.getMessage());
            return false;
        }
    }

    // logging order notification details if the feature flag is enabled
    public void logOrderNotification(Long orderId, Long productId, int quantity, double totalPrice) {
        if (isOrderNotificationsEnabled()) {
            logger.info("🔔 ORDER NOTIFICATION: Order #{} created for Product #{} | Quantity: {} | Total: ${}",
                    orderId, productId, quantity, totalPrice);
        }
    }

    // applying a 15% discount for bulk orders if feature flag is enabled and quantity exceeds 5 items
    public double applyBulkDiscount(double originalTotal, int quantity) {
        if (isBulkOrderDiscountEnabled() && quantity > 5) {
            double discountedTotal = originalTotal * 0.85;
            logger.info("💰 Bulk discount applied: ${} -> ${} (15% off)", originalTotal, discountedTotal);
            return discountedTotal;
        }
        return originalTotal;
    }
}