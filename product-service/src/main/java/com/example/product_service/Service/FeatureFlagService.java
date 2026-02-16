package com.example.product_service.Service;

import io.getunleash.Unleash;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final Unleash unleash;

    public FeatureFlagService(Unleash unleash) {
        this.unleash = unleash;
    }

    public boolean isPremiumPricingEnabled() {
        try {
            return unleash.isEnabled("premium-pricing", false);
        } catch (Exception e) {
            System.err.println("Error checking premium-pricing flag: " + e.getMessage());
            return false;
        }
    }

    public double applyPremiumPricing(double originalPrice) {
        if (isPremiumPricingEnabled()) {
            return originalPrice * 0.9;
        }
        return originalPrice;
    }
}