package dev.rudyevhenii.crypto_aggregator.price_alert.scheduler;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertInMemoryCacheManager;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceAlertExpirationScheduler {

    private final PriceAlertRepository repository;
    private final PriceAlertInMemoryCacheManager inMemoryCacheManager;

    @Scheduled(cron = "${app.price-alets.scheduler.expiration.cron:0 */5 * * * *}")
    public void deactivateExpiredAlerts() {
        List<PriceAlert> priceAlerts = repository.deactivateExpiredAlerts();
        if (CollectionUtils.isEmpty(priceAlerts)) return;

        priceAlerts.forEach(inMemoryCacheManager::removeAlertFromCache);
    }
}
