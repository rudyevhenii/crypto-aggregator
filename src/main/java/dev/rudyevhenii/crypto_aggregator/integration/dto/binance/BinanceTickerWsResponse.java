package dev.rudyevhenii.crypto_aggregator.integration.dto.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceTickerWsResponse(
        @JsonProperty("E") long eventTime,
        @JsonProperty("s") String tradingPair,
        @JsonProperty("P") String priceChangePercent24h,
        @JsonProperty("c") String lastPrice,
        @JsonProperty("h") String high24h,
        @JsonProperty("l") String low24h,
        @JsonProperty("v") String volume24h
) {
}
