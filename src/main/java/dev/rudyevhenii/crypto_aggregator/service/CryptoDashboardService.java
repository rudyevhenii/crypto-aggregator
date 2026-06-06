package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Flux;

public interface CryptoDashboardService {

    Flux<CryptoDashboardDto> streamCryptoPrices(TradingPair symbol);
}
