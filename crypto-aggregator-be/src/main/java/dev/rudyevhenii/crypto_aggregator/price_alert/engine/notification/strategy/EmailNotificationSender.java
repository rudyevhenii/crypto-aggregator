package dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.strategy;

import dev.rudyevhenii.crypto_aggregator.auth.service.UserService;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.notification.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSenderStrategy {

    private final EmailSenderService emailSenderService;
    private final UserService userService;

    @Override
    public void sendNotification(PriceAlert priceAlert, BigDecimal livePrice) {
        UserDetails userDetails = userService.findById(priceAlert.getUserId());
        String userEmail = userDetails.getUsername();

        emailSenderService.sendMessage(userEmail, priceAlert, livePrice);
        log.info("Sending alert email to: {}", userEmail);
    }

    @Override
    public DeliveryMethod getDeliveryMethod() {
        return DeliveryMethod.EMAIL;
    }
}
