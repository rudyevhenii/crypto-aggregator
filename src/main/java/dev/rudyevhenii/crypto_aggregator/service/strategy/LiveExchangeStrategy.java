package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Flux;

public interface LiveExchangeStrategy extends ExchangeStrategy {

    Flux<LivePriceDto> streamPrice(TradingPair tradingPair);

    Flux<ExchangeHealthDto> streamHealth();
}
