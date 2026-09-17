package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import dev.rudyevhenii.crypto_aggregator.core.config.CaffeineCacheConfig;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.strategy.NotificationSenderStrategy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy.ConditionEvaluatorStrategy;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceAlertEngineService {

    private static final String PRICE_ALERTS_CACHE_KEY = "%s:%s";

    private final CaffeineCacheManager caffeineCacheManager;
    private final PriceAlertRepository priceAlertRepository;
    private final Map<ConditionType, ConditionEvaluatorStrategy> conditionEvaluatorStrategies;
    private final Map<DeliveryMethod, NotificationSenderStrategy> notificationSenderStrategies;
    private final PriceAlertCooldownService priceAlertCooldownService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadPriceAlertIntoCache() {
        Map<String, List<PriceAlert>> priceAlertMap = priceAlertRepository.findAllActive().stream()
                .collect(Collectors.groupingBy(
                        priceAlert -> PRICE_ALERTS_CACHE_KEY.formatted(
                                priceAlert.getExchange(),
                                priceAlert.getTradingPair()
                        ),
                        Collectors.toCollection(CopyOnWriteArrayList::new)
                ));
        Cache priceAlertsCache = getPriceAlertsCache();
        priceAlertMap.forEach(priceAlertsCache::putIfAbsent);
    }

    public void addAlertToCache(PriceAlert priceAlert) {
        Cache priceAlertsCache = getPriceAlertsCache();
        CopyOnWriteArrayList<PriceAlert> priceAlerts = getPriceAlertsValueFromCache(priceAlert);

        if (priceAlerts == null) {
            priceAlerts = new CopyOnWriteArrayList<>();
            priceAlertsCache.put(resolvePriceAlertsKey(priceAlert), priceAlerts);
        }
        priceAlerts.add(priceAlert);
    }

    public void updateAlertFromCache(PriceAlert priceAlert) {
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

    public void processNewPrice(LivePriceDto livePriceDto) {
        Cache priceAlertsCache = getPriceAlertsCache();
        CopyOnWriteArrayList<PriceAlert> priceAlerts = priceAlertsCache.get(
                resolvePriceAlertsKey(livePriceDto),
                CopyOnWriteArrayList.class
        );
        if (CollectionUtils.isEmpty(priceAlerts)) return;

        for (PriceAlert priceAlert : priceAlerts) {
            // TODO: Add scheduler to set active status to false when PriceAlert gets expired.
            //  Also it should update caffeine cache to sync with db state
            if (priceAlert.getExpiresAt().isBefore(livePriceDto.timestamp())) {
                continue;
            }
            ConditionPayload conditionPayload = priceAlert.getConditionPayload();
            ConditionEvaluatorStrategy conditionEvaluatorStrategy = conditionEvaluatorStrategies.get(conditionPayload.getConditionType());
            if (conditionEvaluatorStrategy.shouldTrigger(priceAlert, livePriceDto.lastPrice())) {
                for (DeliveryMethod deliveryMethod : priceAlert.getDeliveryMethods()) {
                    sendNotification(priceAlert, livePriceDto.lastPrice(), deliveryMethod);
                }
            }
        }
    }

    private void sendNotification(PriceAlert priceAlert, BigDecimal livePrice, DeliveryMethod deliveryMethod) {
        NotificationSenderStrategy notificationStrategy = notificationSenderStrategies.get(deliveryMethod);
        if (priceAlert.getCooldownMinutes() == null) {
            removeAlertFromCache(priceAlert);
        } else {
            if (priceAlertCooldownService.isPriceAlertSetOnCooldown(priceAlert)) {
                return;
            }
            priceAlertCooldownService.setPriceAlertOnCooldown(priceAlert);
        }
        notificationStrategy.sendNotification(priceAlert, livePrice);
    }

    private CopyOnWriteArrayList<PriceAlert> getPriceAlertsValueFromCache(PriceAlert priceAlert) {
        Cache priceAlertsCache = getPriceAlertsCache();
        return priceAlertsCache.get(resolvePriceAlertsKey(priceAlert), CopyOnWriteArrayList.class);
    }

    private Cache getPriceAlertsCache() {
        return Objects.requireNonNull(caffeineCacheManager.getCache(CaffeineCacheConfig.PRICE_ALERTS));
    }

    private String resolvePriceAlertsKey(PriceAlert priceAlert) {
        return resolveKey(priceAlert.getExchange(), priceAlert.getTradingPair());
    }

    private String resolvePriceAlertsKey(LivePriceDto livePriceDto) {
        return resolveKey(livePriceDto.exchange(), livePriceDto.tradingPair());
    }

    private String resolveKey(Exchange exchange, TradingPair tradingPair) {
        return PRICE_ALERTS_CACHE_KEY.formatted(exchange, tradingPair);
    }
}
