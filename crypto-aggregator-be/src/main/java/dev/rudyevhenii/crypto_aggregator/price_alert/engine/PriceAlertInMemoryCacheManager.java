package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import dev.rudyevhenii.crypto_aggregator.core.config.CaffeineCacheConfig;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class PriceAlertInMemoryCacheManager {

    public static final String PRICE_ALERTS_CACHE_KEY = "%s:%s";

    private final CaffeineCacheManager caffeineCacheManager;

    public void addAlertToCache(PriceAlert priceAlert) {
        Cache priceAlertsCache = getPriceAlertsCache();
        CopyOnWriteArrayList<PriceAlert> priceAlerts = getPriceAlertsValueFromCache(priceAlert);

        if (priceAlerts == null) {
            priceAlerts = new CopyOnWriteArrayList<>();
            priceAlertsCache.put(resolvePriceAlertsKey(priceAlert), priceAlerts);
        }
        priceAlerts.add(priceAlert);
    }

    public void upsertAlertInCache(PriceAlert priceAlert) {
        Cache priceAlertsCache = getPriceAlertsCache();
        CopyOnWriteArrayList<PriceAlert> priceAlerts = getPriceAlertsValueFromCache(priceAlert);

        if (priceAlerts == null) {
            priceAlerts = new CopyOnWriteArrayList<>();
            priceAlertsCache.put(resolvePriceAlertsKey(priceAlert), priceAlerts);
        } else {
            priceAlerts.removeIf(savedPriceAlert -> savedPriceAlert.getId().equals(priceAlert.getId()));
        }
        priceAlerts.add(priceAlert);
    }

    public void updateAlertInCache(PriceAlert priceAlert) {
        CopyOnWriteArrayList<PriceAlert> priceAlerts = getPriceAlertsValueFromCache(priceAlert);

        if (priceAlerts != null) {
            boolean result = priceAlerts.removeIf(savedPriceAlert ->
                    savedPriceAlert.getId().equals(priceAlert.getId()));
            if (result) {
                priceAlerts.add(priceAlert);
            }
        }
    }

    public void removeAlertFromCache(PriceAlert priceAlert) {
        CopyOnWriteArrayList<PriceAlert> priceAlerts = getPriceAlertsValueFromCache(priceAlert);
        if (priceAlerts != null) {
            priceAlerts.removeIf(alert -> alert.getId().equals(priceAlert.getId()));
        }
    }

    public Cache getPriceAlertsCache() {
        return Objects.requireNonNull(caffeineCacheManager.getCache(CaffeineCacheConfig.PRICE_ALERTS));
    }

    public String resolvePriceAlertsKey(LivePriceDto livePriceDto) {
        return resolveKey(livePriceDto.exchange(), livePriceDto.tradingPair());
    }

    public CopyOnWriteArrayList<PriceAlert> getPriceAlertsValueFromCache(PriceAlert priceAlert) {
        Cache priceAlertsCache = getPriceAlertsCache();
        return priceAlertsCache.get(resolvePriceAlertsKey(priceAlert), CopyOnWriteArrayList.class);
    }

    private String resolvePriceAlertsKey(PriceAlert priceAlert) {
        return resolveKey(priceAlert.getExchange(), priceAlert.getTradingPair());
    }

    private String resolveKey(Exchange exchange, TradingPair tradingPair) {
        return PRICE_ALERTS_CACHE_KEY.formatted(exchange, tradingPair);
    }
}
