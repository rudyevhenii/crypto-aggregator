package dev.rudyevhenii.crypto_aggregator.exchange.metadata.model;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import lombok.Builder;

import java.util.List;

@Builder
public record ExchangeMetadataDto(
        Exchange exchange,
        List<TradingPair> supportedPairs,
        List<ChartInterval> supportedIntervals
) {
}
