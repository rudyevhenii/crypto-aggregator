package dev.rudyevhenii.crypto_aggregator.exchange.live.model;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record LivePriceDto(
        Exchange exchange,
        TradingPair tradingPair,
        BigDecimal lastPrice,
        BigDecimal priceChangePercent24h,
        BigDecimal highPrice24h,
        BigDecimal lowPrice24h,
        BigDecimal volume24h,
        Instant timestamp
) {
}
