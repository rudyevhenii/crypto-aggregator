package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeMetadataDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;

import java.util.List;

public interface ExchangeMetadataService {

    List<Exchange> getSupportedExchanges();

    List<TradingPair> getSupportedPairs(Exchange exchange);

    List<ChartInterval> getSupportedIntervals(Exchange exchange);

    List<ExchangeMetadataDto> getAllMetadata();
}
