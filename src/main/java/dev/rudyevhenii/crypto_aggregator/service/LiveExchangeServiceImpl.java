package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.strategy.LiveExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveExchangeServiceImpl implements LiveExchangeService {

    private final Map<Exchange, LiveExchangeStrategy> liveExchangeStrategies;

    @Override
    public Flux<LivePriceDto> streamPriceByExchange(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamAllPrices(exchange);
    }

    @Override
    public Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair pair) {
        return liveExchangeStrategies.get(exchange)
                .streamPrice(exchange, pair);
    }

    @Override
    public Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamHealth(exchange);
    }
}
