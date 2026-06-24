package dev.rudyevhenii.crypto_aggregator.exchange.live;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LiveExchangeService {

    Flux<List<LivePriceDto>> streamAllPrices();

    Flux<LivePriceDto> streamPriceByExchange(Exchange exchange);

    Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair pair);

    Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange);
}
