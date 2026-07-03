package dev.rudyevhenii.crypto_aggregator.exchange.live;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.strategy.LiveExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveExchangeServiceImpl implements LiveExchangeService {

    private static final int BUFFER_DELAY = 500;

    private final Map<Exchange, LiveExchangeStrategy> liveExchangeStrategies;

    @Override
    public Flux<List<LivePriceDto>> streamAllPrices() {
        return Flux.merge(liveExchangeStrategies.entrySet().stream()
                        .map(entry -> entry.getValue().streamPriceByExchange(entry.getKey()))
                        .toList())
                .buffer(Duration.ofMillis(BUFFER_DELAY));
    }

    @Override
    public Flux<LivePriceDto> streamPriceByExchange(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamPriceByExchange(exchange);
    }

    @Override
    public Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair pair) {
        return liveExchangeStrategies.get(exchange)
                .streamSinglePair(exchange, pair);
    }

    @Override
    public Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamExchangeHealth(exchange);
    }
}
