package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractHistoricalExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.service.strategy.model.KlinesRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class CoinbaseHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final String KLINES_URI = "/products/%s/candles";
    private static final ParameterizedTypeReference<List<List<Number>>> TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;

    public CoinbaseHistoricalExchangeStrategy(@Qualifier("coinbaseWebClient") WebClient webClient,
                                              CryptoProperties properties) {
        super(EXCHANGE_NAME, properties);
        this.webClient = webClient;
    }

    @Override
    protected Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor) {
        Duration intervalDuration = request.getInterval().getDuration();
        long startTimeCursor = endTimeCursor.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());
        return Instant.ofEpochSecond(startTimeCursor);
    }

    @Override
    protected URI getKlinesUri(String resolvedTradingPair) {
        return URI.create(KLINES_URI.formatted(resolvedTradingPair));
    }

    @Override
    protected URI resolveKlinesUri(KlinesRequestContext context) {
        return UriComponentsBuilder.fromUri(context.uri())
                .queryParam("granularity", Long.parseLong(context.intervalCode()))
                .queryParam("start", context.startTimeCursor())
                .queryParam("end", context.endTimeCursor())
                .build()
                .toUri();
    }

    @Override
    protected Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(TYPE_REFERENCE)
                .map(this::mapHistoricalPrice)
                .doOnError(error -> log.warn("[{}] Failed to fetch or parse klines from {}. Error: {}",
                        EXCHANGE_NAME.name(), uri, error.getMessage()))
                .onErrorResume(error -> Mono.just(Collections.emptyList()));
    }

    private List<HistoricalPriceDto> mapHistoricalPrice(List<List<Number>> klines) {
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
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
