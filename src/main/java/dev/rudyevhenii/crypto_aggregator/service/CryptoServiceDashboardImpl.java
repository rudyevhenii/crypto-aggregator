package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.service.strategy.CryptoExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptoServiceDashboardImpl implements CryptoDashboardService {

    private final List<CryptoExchangeStrategy> cryptoStrategies;

    @Override
    public Flux<CryptoDashboardDto> streamCryptoPrices() {
        return Flux.interval(Duration.ofSeconds(3))
                .flatMap(tick -> collectPrices())
                .map(this::toDashboardBuilder);
    }

    private Mono<List<CryptoPriceDto>> collectPrices() {
        return Flux.fromIterable(cryptoStrategies)
                .flatMap(strategy -> strategy.streamPrice()
                        .onErrorResume(error -> Mono.empty()))
                .collectList();
    }

    private CryptoDashboardDto toDashboardBuilder(List<CryptoPriceDto> prices) {
        return CryptoDashboardDto.builder()
                .cryptoPrices(prices)
                .updateTime(Instant.now())
                .build();
    }
}
