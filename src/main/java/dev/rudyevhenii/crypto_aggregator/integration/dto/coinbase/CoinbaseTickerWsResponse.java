package dev.rudyevhenii.crypto_aggregator.integration.dto.coinbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinbaseTickerWsResponse(
        @JsonProperty("product_id") String tradingPair,
        @JsonProperty("price") String price,
        @JsonProperty("time") Instant timestamp
) {
}
