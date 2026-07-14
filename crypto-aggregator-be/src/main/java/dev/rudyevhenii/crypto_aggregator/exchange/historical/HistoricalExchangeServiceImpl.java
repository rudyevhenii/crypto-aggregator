package dev.rudyevhenii.crypto_aggregator.exchange.historical;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy.HistoricalExchangeStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.HISTORICAL_PRICES_ARCHIVE_CACHE;
import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.HISTORICAL_PRICES_LIVE_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalExchangeServiceImpl implements HistoricalExchangeService {

    private final Map<Exchange, HistoricalExchangeStrategy> liveExchangeStrategies;

    @Override
    @Caching(cacheable = {
            @Cacheable(
                    value = HISTORICAL_PRICES_LIVE_CACHE,
                    key = "{#exchange.name(), #request.tradingPair.name(), #request.chartInterval.name(), #request.limit, #request.endTimeCursor}",
                    condition = """
                            #request.endTimeCursor == null &&
                            !{'ONE_SECOND', 'ONE_MINUTE', 'THREE_MINUTES', 'FIVE_MINUTES'}.contains(#request.chartInterval.name())
                            """
            ),
            @Cacheable(
                    value = HISTORICAL_PRICES_ARCHIVE_CACHE,
                    key = "{#exchange.name(), #request.tradingPair.name(), #request.chartInterval.name(), #request.limit, #request.endTimeCursor}",
                    condition = "#request.endTimeCursor != null && #request.chartInterval.name() != 'ONE_SECOND'"
            )
    })
    public List<HistoricalPriceDto> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request) {
        log.debug("Requesting historical prices for exchange [{}] with request: {}", exchange, request);
        return liveExchangeStrategies.get(exchange)
                .fetchHistoricalData(request);
    }

    @Override
    @Cacheable(value = HISTORICAL_PRICES_LIVE_CACHE, key = "{#exchange.name(), #tradingPairs.toString()}",
            unless = "#result == null || #result.isEmpty()")
    public List<Ticker24hDto> get24hTickersByExchange(Exchange exchange, List<TradingPair> tradingPairs) {
        log.info("Requesting 24h tickers for exchange [{}] with tradingPairs: {}", exchange, tradingPairs.toString());
        return liveExchangeStrategies.get(exchange)
                .fetch24hTickers(tradingPairs);
    }

    @Override
    @Cacheable(value = HISTORICAL_PRICES_LIVE_CACHE, key = "{#exchange.name(), #pair.name()}")
    public Ticker24hDto get24hTickerForPair(Exchange exchange, TradingPair pair) {
        log.info("Requesting 24h ticker for pair [{}] on exchange [{}]", pair, exchange);
        return liveExchangeStrategies.get(exchange)
                .fetch24hTicker(pair);
    }
}
