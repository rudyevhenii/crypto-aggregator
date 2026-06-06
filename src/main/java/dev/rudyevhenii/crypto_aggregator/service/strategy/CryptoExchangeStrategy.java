package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Mono;

public interface CryptoExchangeStrategy {

    Mono<CryptoPriceDto> streamPrice(TradingPair symbol);

    Exchange getExchangeType();
}
