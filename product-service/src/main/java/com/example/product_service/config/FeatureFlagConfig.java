package com.example.product_service.config;

import io.getunleash.Unleash;
import io.getunleash.DefaultUnleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureFlagConfig {

    // application name
    @Value("${unleash.app-name:product-service}")
    private String appName;

    // identifier
    @Value("${unleash.instance-id:${HOSTNAME:local}}")
    private String instanceId;

    // runtime environment
    @Value("${unleash.environment:development}")
    private String environment;

    // api base url
    @Value("${unleash.api-url:http://localhost:4242/api}")
    private String apiUrl;

    // creating and configuring client bean
    @Bean
    public Unleash unleash() {
        UnleashConfig config = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceId)
                .environment(environment)
                .unleashAPI(apiUrl)
                .apiKey("*:*.unleash-default-token")
                .fetchTogglesInterval(10)
                .disableMetrics()
                .build();

        return new DefaultUnleash(config);
    }
}