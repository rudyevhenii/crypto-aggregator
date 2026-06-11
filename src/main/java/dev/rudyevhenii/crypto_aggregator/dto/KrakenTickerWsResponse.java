package dev.rudyevhenii.crypto_aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KrakenTickerWsResponse(
        @JsonProperty("data") List<KrakenTickerData> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KrakenTickerData(
            @JsonProperty("symbol") String tradingPair,
            @JsonProperty("last") BigDecimal price,
            @JsonProperty("eventTime") Instant timestamp
    ) {
    }
}