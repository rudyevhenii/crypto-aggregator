package dev.rudyevhenii.crypto_aggregator.integration.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BinanceTicker24hResponse(
        @JsonProperty("symbol") String tradingPair,
        @JsonProperty("priceChangePercent") BigDecimal priceChangePercent24h,
        BigDecimal lastPrice,
        @JsonProperty("highPrice") BigDecimal highPrice24h,
        @JsonProperty("lowPrice") BigDecimal lowPrice24h,
        @JsonProperty("volume24h") BigDecimal volume24h
) {
}
