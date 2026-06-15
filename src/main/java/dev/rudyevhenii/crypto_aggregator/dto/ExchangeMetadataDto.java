package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.Builder;

import java.util.List;

@Builder
public record ExchangeMetadataDto(
        Exchange exchange,
        List<TradingPair> supportedPairs,
        List<ChartInterval> supportedIntervals
) {
}
