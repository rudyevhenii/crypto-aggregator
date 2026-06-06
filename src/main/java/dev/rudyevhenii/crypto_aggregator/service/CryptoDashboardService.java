package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import reactor.core.publisher.Flux;

public interface CryptoDashboardService {

    Flux<CryptoDashboardDto> streamCryptoPrices();
}
