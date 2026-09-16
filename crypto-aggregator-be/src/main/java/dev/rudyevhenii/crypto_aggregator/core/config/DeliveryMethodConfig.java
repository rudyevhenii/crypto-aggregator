package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.strategy.NotificationSenderStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DeliveryMethodConfig {

    @Bean
    public Map<DeliveryMethod, NotificationSenderStrategy> notificationSenderStrategies(List<NotificationSenderStrategy> notificationSenders) {
        return notificationSenders.stream()
                .collect(Collectors.toMap(NotificationSenderStrategy::getDeliveryMethod, Function.identity()));
    }
}
