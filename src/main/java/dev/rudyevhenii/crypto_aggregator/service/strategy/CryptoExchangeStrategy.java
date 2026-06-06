package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import reactor.core.publisher.Mono;

public interface CryptoExchangeStrategy {

    Mono<CryptoPriceDto> streamPrice();

    Exchange getExchangeType();
}
