package dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertStateStorageService;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.TrailingPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

@Component
@RequiredArgsConstructor
public class TrailingDropConditionEvaluator implements ConditionEvaluatorStrategy {

    private final PriceAlertStateStorageService storageService;

    @Override
    public boolean shouldTrigger(PriceAlert priceAlert, BigDecimal livePrice) {
        BigDecimal allTimeHighPrice = storageService.getPreviousPrice(priceAlert.getId());
        TrailingPayload payload = (TrailingPayload) priceAlert.getConditionPayload();

        if (allTimeHighPrice == null) {
            allTimeHighPrice = payload.getReferencePrice();
            storageService.updateExtremumPriceAsync(priceAlert.getId(), allTimeHighPrice);
        }
        if (allTimeHighPrice.compareTo(livePrice) < 0) {
            storageService.updateExtremumPriceAsync(priceAlert.getId(), livePrice);
            return false;
        }
        BigDecimal percentage = payload.getTrailingPercentage().movePointLeft(2);
        BigDecimal activationPrice = allTimeHighPrice.subtract(allTimeHighPrice.multiply(percentage, MathContext.DECIMAL64));

        return livePrice.compareTo(activationPrice) <= 0;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.TRAILING_DROP;
    }
}
