package dev.rudyevhenii.crypto_aggregator.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class ExchangeUtils {

    private final int PERCENT_TO_MULTIPLIER_SHIFT = 2;

    public BigDecimal calculatePercentChange(BigDecimal lastPrice, BigDecimal openPrice) {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return lastPrice.subtract(openPrice)
                .divide(openPrice, 6, RoundingMode.HALF_EVEN)
                .movePointRight(PERCENT_TO_MULTIPLIER_SHIFT)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

}
