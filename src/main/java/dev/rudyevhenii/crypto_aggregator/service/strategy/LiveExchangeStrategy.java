package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Flux;

public interface LiveExchangeStrategy extends ExchangeStrategy {

    Flux<CryptoPriceDto> streamPrice(TradingPair tradingPair);

}
