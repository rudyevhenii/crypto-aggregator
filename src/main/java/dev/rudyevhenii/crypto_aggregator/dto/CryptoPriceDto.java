package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record CryptoPriceDto(
        Exchange exchange,
        TradingPair tradingPair,
        BigDecimal price,
        Instant timestamp
) {
}
