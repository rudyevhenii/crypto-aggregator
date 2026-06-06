package dev.rudyevhenii.crypto_aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KrakenResponse(
        Map<String, KrakenTicker> result
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KrakenTicker(
            List<String> c
    ) {
    }
}