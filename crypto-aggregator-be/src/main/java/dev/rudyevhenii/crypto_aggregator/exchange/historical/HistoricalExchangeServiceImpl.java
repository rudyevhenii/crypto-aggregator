package dev.rudyevhenii.crypto_aggregator.exchange.historical;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy.HistoricalExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoricalExchangeServiceImpl implements HistoricalExchangeService {

    private final Map<Exchange, HistoricalExchangeStrategy> liveExchangeStrategies;

    @Override
    public Mono<List<HistoricalPriceDto>> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request) {
        return liveExchangeStrategies.get(exchange)
                .fetchHistoricalData(request);
    }

    @Override
    public Mono<List<Ticker24hDto>> get24hTickersByExchange(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .fetch24hTickers();
    }

    @Override
    public Mono<Ticker24hDto> get24hTickerForPair(Exchange exchange, TradingPair pair) {
        return liveExchangeStrategies.get(exchange)
                .fetch24hTicker(pair);
    }
}
