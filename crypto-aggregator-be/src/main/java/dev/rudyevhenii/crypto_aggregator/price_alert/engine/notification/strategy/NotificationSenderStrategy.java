package dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import org.springframework.scheduling.annotation.Async;

public interface NotificationSenderStrategy {

    @Async("virtualExecutor")
    void sendNotification(PriceAlert priceAlert);

    DeliveryMethod getDeliveryMethod();
}
