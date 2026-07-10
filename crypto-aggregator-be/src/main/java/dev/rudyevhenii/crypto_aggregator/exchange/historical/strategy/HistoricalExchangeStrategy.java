package dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.ExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;

import java.util.List;

public interface HistoricalExchangeStrategy extends ExchangeStrategy {

    List<HistoricalPriceDto> fetchHistoricalData(HistoricalPriceRequest request);

    List<Ticker24hDto> fetch24hTickers();

    Ticker24hDto fetch24hTicker(TradingPair pair);
}
