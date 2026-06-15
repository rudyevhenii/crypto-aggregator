package dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CoinbaseTicker24hResponse(
        @JsonProperty("open") BigDecimal openPrice24h,
        @JsonProperty("high") BigDecimal highPrice24h,
        @JsonProperty("low") BigDecimal lowPrice24h,
        @JsonProperty("volume24h") BigDecimal volume24h,
        @JsonProperty("last") BigDecimal lastPrice
) {
}
