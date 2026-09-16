package dev.rudyevhenii.crypto_aggregator.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineCacheConfig {

    public static final String PRICE_ALERTS = "priceAlerts";

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PRICE_ALERTS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100));

        return cacheManager;
    }
}
