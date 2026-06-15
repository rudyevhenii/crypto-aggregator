package dev.rudyevhenii.crypto_aggregator.integration.kraken.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KrakenTicker24hResponse(
        Map<String, KrakenTickerData> result
) {
    public record KrakenTickerData(
            @JsonProperty("c") List<String> lastPrice,
            @JsonProperty("v") List<String> volume24h,
            @JsonProperty("l") List<String> lowPrice24h,
            @JsonProperty("h") List<String> highPrice24h,
            @JsonProperty("o") BigDecimal openPrice24h
    ) {
    }
}
