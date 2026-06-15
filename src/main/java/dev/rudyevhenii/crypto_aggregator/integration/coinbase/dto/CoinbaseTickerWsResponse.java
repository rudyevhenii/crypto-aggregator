package dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinbaseTickerWsResponse(
        @JsonProperty("product_id") String tradingPair,
        @JsonProperty("lastPrice") BigDecimal lastPrice,
        @JsonProperty("open_24h") BigDecimal openPrice24h,
        @JsonProperty("volume_24h") BigDecimal volume24h,
        @JsonProperty("low_24h") BigDecimal lowPrice24h,
        @JsonProperty("high_24h") BigDecimal highPrice24h,
        @JsonProperty("time") Instant timestamp
) {
}
