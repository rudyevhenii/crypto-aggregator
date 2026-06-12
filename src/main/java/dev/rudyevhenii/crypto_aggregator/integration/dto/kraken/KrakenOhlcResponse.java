package dev.rudyevhenii.crypto_aggregator.integration.dto.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KrakenOhlcResponse(
        JsonNode result
) {
}
