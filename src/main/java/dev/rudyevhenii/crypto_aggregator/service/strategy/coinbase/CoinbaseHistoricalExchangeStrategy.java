package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractHistoricalExchangeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class CoinbaseHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final String KLINES_URI = "/products/%s/candles?granularity=%d&start=%s&end=%s";
    private static final ParameterizedTypeReference<List<List<Number>>> PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private final WebClient exchangeWebClient;
    private final CryptoProperties properties;

    public CoinbaseHistoricalExchangeStrategy(@Qualifier("coinbaseExchangeWebClient") WebClient exchangeWebClient,
                                              CryptoProperties properties) {
        super(exchangeWebClient, EXCHANGE_NAME);
        this.exchangeWebClient = exchangeWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalPrices(HistoricalPriceRequest request) {
        String resolvedTradingPair = getTradingPairCode(request.getTradingPair());
        long intervalInSeconds = Long.parseLong(getAndValidateExchangeInterval(request.getInterval()));

        Instant effectiveEndTime = request.getCursor() == null
                ? Instant.now()
                : request.getCursor().minusMillis(1);

        Duration intervalDuration = request.getInterval().getDuration();
        long startTime = effectiveEndTime.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());

        return exchangeWebClient.get()
                .uri(KLINES_URI.formatted(resolvedTradingPair, intervalInSeconds,
                        Instant.ofEpochSecond(startTime), effectiveEndTime))
                .retrieve()
                .bodyToMono(PARAMETERIZED_TYPE_REFERENCE)
                .onErrorStop()
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        getExchangeType().name(), error.getMessage()))
                .map(this::toCoinbaseKlines);
    }

    private List<HistoricalPriceDto> toCoinbaseKlines(List<List<Number>> klines) {
        if (klines == null || klines.isEmpty()) {
            return Collections.emptyList();
        }

        return klines.stream()
                .map(kline -> {
                    long timeInSeconds = kline.get(0).longValue();
                    Instant openTime = Instant.ofEpochSecond(timeInSeconds);

                    return HistoricalPriceDto.builder()
                            .openTime(openTime)
                            .low(new BigDecimal(kline.get(1).toString()))
                            .high(new BigDecimal(kline.get(2).toString()))
                            .open(new BigDecimal(kline.get(3).toString()))
                            .close(new BigDecimal(kline.get(4).toString()))
                            .volume(new BigDecimal(kline.get(5).toString()))
                            .build();
                })
                .sorted(Comparator.comparing(HistoricalPriceDto::openTime))
                .toList();
    }

    @Override
    public CryptoProperties getCryptoProperties() {
        return properties;
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
