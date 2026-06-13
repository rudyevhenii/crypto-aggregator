package dev.rudyevhenii.crypto_aggregator.integration.dto.coinbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinbaseTickerWsResponse(
        @JsonProperty("product_id") String tradingPair,
        @JsonProperty("price") String lastPrice,
        @JsonProperty("open_24h") String open24h,
        @JsonProperty("volume_24h") String volume24h,
        @JsonProperty("low_24h") String low24h,
        @JsonProperty("high_24h") String high24h,
        @JsonProperty("time") Instant timestamp
) {
}
