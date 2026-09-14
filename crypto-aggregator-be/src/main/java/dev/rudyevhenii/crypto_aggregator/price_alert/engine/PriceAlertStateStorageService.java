package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class PriceAlertStateStorageService {

    private static final String KEY_PREFIX = "alert:prev_price:";

    private static final Duration EXTREMUM_PRICE_TTL = Duration.ofDays(30);
    private static final Duration PREVIOUS_PRICE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ExecutorService virtualExecutor;

    public void updateExtremumPriceAsync(UUID alertId, BigDecimal price) {
        writeAlertPrice(alertId, price, EXTREMUM_PRICE_TTL);
    }

    public void updatePreviousPriceAsync(UUID alertId, BigDecimal price) {
        writeAlertPrice(alertId, price, PREVIOUS_PRICE_TTL);
    }

    private void writeAlertPrice(UUID alertId, BigDecimal price, Duration ttl) {
        virtualExecutor.submit(() ->
                redisTemplate.opsForValue().set(KEY_PREFIX + alertId, price.toPlainString(), ttl)
        );
    }

    public BigDecimal getPreviousPrice(UUID alertId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + alertId);
        return value != null ? new BigDecimal(value) : null;
    }
}
