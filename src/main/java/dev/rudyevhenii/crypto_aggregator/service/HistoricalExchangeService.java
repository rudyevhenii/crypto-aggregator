package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HistoricalExchangeService {

    Mono<List<HistoricalPriceDto>> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request);

    Mono<List<Ticker24hDto>> get24hTickersByExchange(Exchange exchange);

    Mono<Ticker24hDto> get24hTickerForPair(Exchange exchange, TradingPair pair);
}
