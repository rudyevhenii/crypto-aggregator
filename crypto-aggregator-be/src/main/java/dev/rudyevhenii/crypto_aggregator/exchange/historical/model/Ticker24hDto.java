package dev.rudyevhenii.crypto_aggregator.exchange.historical.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record Ticker24hDto(
        Exchange exchange,
        TradingPair tradingPair,
        BigDecimal lastPrice,
        BigDecimal priceChangePercent24h,
        BigDecimal high24h,
        BigDecimal low24h,
        BigDecimal volume24h
) {
}
