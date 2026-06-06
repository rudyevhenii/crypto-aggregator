package dev.rudyevhenii.crypto_aggregator.dto;

import java.math.BigDecimal;

public record BinanceResponse(
        String symbol,
        BigDecimal price
) {
}
