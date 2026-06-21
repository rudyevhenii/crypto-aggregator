package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Flux;

public interface LiveExchangeService {

    Flux<LivePriceDto> streamPriceByExchange(Exchange exchange);

    Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair pair);

    Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange);
}
