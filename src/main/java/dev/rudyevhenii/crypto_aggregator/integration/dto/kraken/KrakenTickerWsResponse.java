package dev.rudyevhenii.crypto_aggregator.integration.dto.kraken;

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
            @JsonProperty("last") BigDecimal lastPrice,
            @JsonProperty("eventTime") Instant timestamp,
            @JsonProperty("change_pct") BigDecimal priceChangePercent24h,
            @JsonProperty("high") BigDecimal high24h,
            @JsonProperty("low") BigDecimal low24h,
            @JsonProperty("volume") BigDecimal volume24h
    ) {
    }
}