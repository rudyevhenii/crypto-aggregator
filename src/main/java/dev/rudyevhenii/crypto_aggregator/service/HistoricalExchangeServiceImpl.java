package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.strategy.HistoricalExchangeStrategy;
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
    public Mono<List<HistoricalPriceDto>> getHistoricalPrices(HistoricalPriceRequest request) {
        return liveExchangeStrategies.get(request.getExchange())
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
