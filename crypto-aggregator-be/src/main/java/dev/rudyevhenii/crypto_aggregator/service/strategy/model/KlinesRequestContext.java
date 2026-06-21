package dev.rudyevhenii.crypto_aggregator.service.strategy.model;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import lombok.Builder;

import java.net.URI;
import java.time.Instant;

@Builder
public record KlinesRequestContext(
        URI uri,
        String tradingPair,
        String intervalCode,
        Instant endTimeCursor,
        Instant startTimeCursor,
        HistoricalPriceRequest originalRequest
) {
}
