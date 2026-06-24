package dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.ExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HistoricalExchangeStrategy extends ExchangeStrategy {

    Mono<List<HistoricalPriceDto>> fetchHistoricalData(HistoricalPriceRequest request);

    Mono<List<Ticker24hDto>> fetch24hTickers();

    Mono<Ticker24hDto> fetch24hTicker(TradingPair pair);
}
