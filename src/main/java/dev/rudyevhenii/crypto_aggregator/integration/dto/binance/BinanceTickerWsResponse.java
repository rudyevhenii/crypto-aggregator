package dev.rudyevhenii.crypto_aggregator.integration.dto.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceTickerWsResponse(
        @JsonProperty("s") String tradingPair,
        @JsonProperty("c") String price,
        @JsonProperty("E") long eventTime
) {
}
