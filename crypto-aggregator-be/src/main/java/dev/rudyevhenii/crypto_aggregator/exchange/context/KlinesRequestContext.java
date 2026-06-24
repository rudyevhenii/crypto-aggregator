package dev.rudyevhenii.crypto_aggregator.exchange.context;

import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
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
