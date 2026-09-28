package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceAlertStateStorageService {

    private static final String PREV_PRICE_KEY_PREFIX = "priceAlerts:previousPrice:";
    private static final String EXTREMUM_PRICE_KEY_PREFIX = "priceAlerts:extremumPrice:";

    private static final Duration EXTREMUM_PRICE_TTL = Duration.ofDays(30);
    private static final Duration PREVIOUS_PRICE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void updateExtremumPrice(UUID alertId, BigDecimal price) {
        writeAlertPrice(EXTREMUM_PRICE_KEY_PREFIX + alertId, price, EXTREMUM_PRICE_TTL);
    }

    public void updatePreviousPrice(UUID alertId, BigDecimal price) {
        writeAlertPrice(PREV_PRICE_KEY_PREFIX + alertId, price, PREVIOUS_PRICE_TTL);
    }

    private void writeAlertPrice(String key, BigDecimal price, Duration ttl) {
        redisTemplate.opsForValue().set(key, price.toPlainString(), ttl);
    }

    public BigDecimal getPreviousPrice(UUID alertId) {
        String value = redisTemplate.opsForValue().get(PREV_PRICE_KEY_PREFIX + alertId);
        return value != null ? new BigDecimal(value) : null;
    }
}
