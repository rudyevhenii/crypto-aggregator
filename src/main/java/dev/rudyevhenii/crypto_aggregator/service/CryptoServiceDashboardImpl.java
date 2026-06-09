package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.strategy.CryptoExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CryptoServiceDashboardImpl implements CryptoDashboardService {

    private static final int INTERVAL = 3;
    private final Map<Exchange, CryptoExchangeStrategy> exchangeStrategies;

    @Override
    public Flux<CryptoDashboardDto> streamAllPrices() {
        return Flux.interval(Duration.ofSeconds(INTERVAL))
                .flatMap(tick -> collectAllPrices());
    }

    @Override
    public Flux<CryptoDashboardDto> streamPricesByPair(TradingPair tradingPair) {
        return Flux.interval(Duration.ofSeconds(INTERVAL))
                .flatMap(tick -> collectPrices(tradingPair))
                .map(this::toDashboardBuilder);
    }

    @Override
    public Flux<CryptoDashboardDto> streamPricesByExchange(Exchange exchange) {
        return Flux.interval(Duration.ofSeconds(INTERVAL))
                .flatMap(tick -> fetchExchangePrices(exchangeStrategies.get(exchange)));
    }

    @Override
    public Flux<CryptoDashboardDto> streamSinglePairOnExchange(Exchange exchange, TradingPair tradingPair) {
        return Flux.interval(Duration.ofSeconds(INTERVAL))
                .flatMap(tick -> Flux.just(tradingPair)
                        .flatMap(pair -> fetchExchangePrices(exchangeStrategies.get(exchange), pair)));
    }

    @Override
    public Mono<List<HistoricalPriceDto>> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request) {
        return exchangeStrategies.get(exchange).fetchHistoricalPrices(request);
    }

    private Flux<CryptoDashboardDto> collectAllPrices() {
        return Flux.fromIterable(exchangeStrategies.values())
                .flatMap(strategy -> fetchExchangePrices(strategy, TradingPair.values()));
    }

    private Mono<CryptoDashboardDto> fetchExchangePrices(CryptoExchangeStrategy strategy, TradingPair... tradingPairs) {
        return Flux.fromIterable(Arrays.asList(tradingPairs))
                .flatMap(tradingPair -> strategy.streamPrice(tradingPair)
                        .onErrorResume(error -> Mono.empty()))
                .collectList()
                .map(this::toDashboardBuilder);
    }

    private Mono<List<CryptoPriceDto>> collectPrices(TradingPair tradingPair) {
        return Flux.fromIterable(exchangeStrategies.values())
                .flatMap(strategy -> strategy.streamPrice(tradingPair)
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
