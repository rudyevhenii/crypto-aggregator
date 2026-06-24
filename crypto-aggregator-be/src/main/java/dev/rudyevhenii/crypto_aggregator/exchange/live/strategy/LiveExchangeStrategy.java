package dev.rudyevhenii.crypto_aggregator.exchange.live.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.ExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import reactor.core.publisher.Flux;

public interface LiveExchangeStrategy extends ExchangeStrategy {

    Flux<LivePriceDto> streamPriceByExchange(Exchange exchange);

    Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair tradingPair);

    Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange);
}
