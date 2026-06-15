package dev.rudyevhenii.crypto_aggregator.integration.binance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceTickerWsResponse(
        @JsonProperty("E") long eventTime,
        @JsonProperty("s") String tradingPair,
        @JsonProperty("P") BigDecimal priceChangePercent24h,
        @JsonProperty("c") BigDecimal lastPrice,
        @JsonProperty("h") BigDecimal high24h,
        @JsonProperty("l") BigDecimal low24h,
        @JsonProperty("v") BigDecimal volume24h
) {
}
