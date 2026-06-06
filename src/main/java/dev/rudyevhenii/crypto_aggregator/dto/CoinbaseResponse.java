package dev.rudyevhenii.crypto_aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CoinbaseResponse(
        CoinbaseData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoinbaseData(
            @JsonProperty("amount")
            BigDecimal price
    ) {
    }
}
