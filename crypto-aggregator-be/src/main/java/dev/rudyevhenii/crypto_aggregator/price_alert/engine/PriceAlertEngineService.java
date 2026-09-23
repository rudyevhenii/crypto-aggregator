package dev.rudyevhenii.crypto_aggregator.price_alert.engine;

import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.OneTimeTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.RecurringTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.strategy.NotificationSenderStrategy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy.ConditionEvaluatorStrategy;
import dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertLogService;
import dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertEngineService {

    private final PriceAlertService priceAlertService;
    private final Map<ConditionType, ConditionEvaluatorStrategy> conditionEvaluatorStrategies;
    private final Map<DeliveryMethod, NotificationSenderStrategy> notificationSenderStrategies;
    private final PriceAlertCooldownService priceAlertCooldownService;
    private final PriceAlertInMemoryCacheManager inMemoryCacheManager;
    private final ExecutorService virtualExecutor;
    private final Sinks.Many<LivePriceDto> priceSink;
    private final PriceAlertLogService priceAlertLogService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadPriceAlertIntoCache() {
        Map<String, List<PriceAlert>> priceAlertMap = priceAlertService.getAllActive().stream()
                .collect(Collectors.groupingBy(
                        priceAlert -> PriceAlertInMemoryCacheManager.PRICE_ALERTS_CACHE_KEY.formatted(
                                priceAlert.getExchange(),
                                priceAlert.getTradingPair()
                        ),
                        Collectors.toCollection(CopyOnWriteArrayList::new)
                ));
        Cache priceAlertsCache = inMemoryCacheManager.getPriceAlertsCache();
        priceAlertMap.forEach(priceAlertsCache::putIfAbsent);

        priceSink.asFlux()
                .publishOn(Schedulers.fromExecutor(virtualExecutor))
                .onErrorContinue((err, failedPriceDto) ->
                        log.error("Error during price processing {}: {}", failedPriceDto, err.getMessage(), err))
                .subscribe(
                        this::processNewPrice,
                        err -> log.error("Error processing price alerts", err)
                );
    }

    public void processNewPrice(LivePriceDto livePriceDto) {
        Cache priceAlertsCache = inMemoryCacheManager.getPriceAlertsCache();
        CopyOnWriteArrayList<PriceAlert> priceAlerts = priceAlertsCache.get(
                inMemoryCacheManager.resolvePriceAlertsKey(livePriceDto),
                CopyOnWriteArrayList.class
        );
        if (CollectionUtils.isEmpty(priceAlerts)) return;

        for (PriceAlert priceAlert : priceAlerts) {
            if (priceAlert.getExpiresAt() == null || livePriceDto.timestamp().isBefore(priceAlert.getExpiresAt())) {
                ConditionPayload conditionPayload = priceAlert.getConditionPayload();
                ConditionEvaluatorStrategy conditionEvaluatorStrategy = conditionEvaluatorStrategies.get(conditionPayload.getConditionType());
                if (conditionEvaluatorStrategy.shouldTrigger(priceAlert, livePriceDto.lastPrice())) {
                    boolean notificationSent = false;
                    for (DeliveryMethod deliveryMethod : priceAlert.getDeliveryMethods()) {
                        notificationSent = isNotificationSent(priceAlert, livePriceDto.lastPrice(), deliveryMethod);
                    }
                    if (notificationSent) priceAlertLogService.create(priceAlert, livePriceDto.lastPrice());
                }
            }
        }
    }

    private boolean isNotificationSent(PriceAlert priceAlert, BigDecimal livePrice, DeliveryMethod deliveryMethod) {
        NotificationSenderStrategy notificationStrategy = notificationSenderStrategies.get(deliveryMethod);
        if (priceAlert.getTriggerPolicy() instanceof OneTimeTriggerPolicy oneTimeTriggerPolicy) {
            inMemoryCacheManager.removeAlertFromCache(priceAlert);
            priceAlertService.deactivateForUser(priceAlert.getUserId(), priceAlert.getId());
        } else if (priceAlert.getTriggerPolicy() instanceof RecurringTriggerPolicy recurringTriggerPolicy) {
            if (!priceAlertCooldownService.isPriceAlertSetOnCooldown(priceAlert)) {
                priceAlertCooldownService.setPriceAlertOnCooldown(priceAlert, recurringTriggerPolicy.getCooldownMinutes());
            } else {
                return false;
            }
        }
        notificationStrategy.sendNotification(priceAlert, livePrice);
        return true;
    }
}
