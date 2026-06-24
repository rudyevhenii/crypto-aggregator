package dev.rudyevhenii.crypto_aggregator.exchange.metadata;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.metadata.model.ExchangeMetadataDto;

import java.util.List;

public interface ExchangeMetadataService {

    List<Exchange> getSupportedExchanges();

    List<TradingPair> getSupportedPairs(Exchange exchange);

    List<ChartInterval> getSupportedIntervals(Exchange exchange);

    List<ExchangeMetadataDto> getAllMetadata();
}
