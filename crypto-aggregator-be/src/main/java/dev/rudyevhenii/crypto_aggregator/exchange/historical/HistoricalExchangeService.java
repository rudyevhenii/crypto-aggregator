package dev.rudyevhenii.crypto_aggregator.exchange.historical;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HistoricalExchangeService {

    Mono<List<HistoricalPriceDto>> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request);

    Mono<List<Ticker24hDto>> get24hTickersByExchange(Exchange exchange);

    Mono<Ticker24hDto> get24hTickerForPair(Exchange exchange, TradingPair pair);
}
