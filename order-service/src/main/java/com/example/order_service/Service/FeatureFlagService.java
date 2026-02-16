package com.example.order_service.Service;

import io.getunleash.Unleash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);
    private final Unleash unleash;

    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    public boolean isOrderNotificationsEnabled() {
        try {
            return unleash.isEnabled("order-notifications", false);
        } catch (Exception e) {
            logger.error("Error checking order-notifications flag: {}", e.getMessage());
            return false;
        }
    }


    public boolean isBulkOrderDiscountEnabled() {
        try {
            return unleash.isEnabled("bulk-order-discount", false);
        } catch (Exception e) {
            logger.error("Error checking bulk-order-discount flag: {}", e.getMessage());
            return false;
        }
    }

    public void logOrderNotification(Long orderId, Long productId, int quantity, double totalPrice) {
        if (isOrderNotificationsEnabled()) {
            logger.info("🔔 ORDER NOTIFICATION: Order #{} created for Product #{} | Quantity: {} | Total: ${}",
                    orderId, productId, quantity, totalPrice);
        }
    }

    public double applyBulkDiscount(double originalTotal, int quantity) {
        if (isBulkOrderDiscountEnabled() && quantity > 5) {
            double discountedTotal = originalTotal * 0.85;
            logger.info("💰 Bulk discount applied: ${} -> ${} (15% off)", originalTotal, discountedTotal);
            return discountedTotal;
        }
        return originalTotal;
    }
}