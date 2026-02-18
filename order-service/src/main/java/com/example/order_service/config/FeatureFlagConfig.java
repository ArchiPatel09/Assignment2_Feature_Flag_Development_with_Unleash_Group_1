package com.example.order_service.config;

import io.getunleash.Unleash;
import io.getunleash.DefaultUnleash;
import io.getunleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureFlagConfig {

    // logger for debugging and monitoring unleash initialization
    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagConfig.class);

    // name of the application registered in unleash
    @Value("${unleash.app-name:order-service}")
    private String appName;

    // unique instance ID
    @Value("${unleash.instance-id:${HOSTNAME:local}}")
    private String instanceId;

    // environment in which the application is runnign
    @Value("${unleash.environment:development}")
    private String environment;

    // api url
    @Value("${unleash.api-url:http://localhost:4242/api}")
    private String apiUrl;

    // api token used to authenticate
    @Value("${unleash.api-token:default:development.unleash-default-token}")
    private String apiToken;

    // creating and configuring the unleash client bean
    @Bean
    public Unleash unleash() {
        logger.info("Initializing Unleash client with API URL: {}", apiUrl);

        // building unleash configuration
        UnleashConfig config = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceId)
                .environment(environment)
                .unleashAPI(apiUrl)
                .apiKey("*:*.unleash-default-token")
                .fetchTogglesInterval(10)
                .disableMetrics()  
                .build();

        System.setProperty("UNLEASH_API_TOKEN", apiToken);

        return new DefaultUnleash(config);
    }

    // utility method to mask sensitive API tokens
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}