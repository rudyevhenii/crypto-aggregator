package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PriceAlertCooldownService {

    private static final String PRICE_ALERT_COOLDOWN_KEY = "priceAlerts:cooldown:";

    private final RedisTemplate<String, PriceAlert> redisTemplate;

    public void setPriceAlertOnCooldown(PriceAlert priceAlert, int cooldownMinutes) {
        redisTemplate.opsForValue().setIfAbsent(
                PRICE_ALERT_COOLDOWN_KEY + priceAlert.getId(), priceAlert,
                Duration.ofMinutes(cooldownMinutes)
        );
    }

    public boolean isPriceAlertSetOnCooldown(PriceAlert priceAlert) {
        PriceAlert cachedPriceAlert = redisTemplate.opsForValue().get(
                PRICE_ALERT_COOLDOWN_KEY + priceAlert.getId()
        );
        return cachedPriceAlert != null;
    }
}
