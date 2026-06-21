package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record Ticker24hDto(
        Exchange exchange,
        TradingPair tradingPair,
        BigDecimal lastPrice,
        BigDecimal priceChangePercent24h,
        BigDecimal high24h,
        BigDecimal low24h,
        BigDecimal volume24h,
        Instant timestamp
) {
}
