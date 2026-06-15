package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exception.UnsupportedIntervalException;
import dev.rudyevhenii.crypto_aggregator.service.strategy.model.KlinesRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

@Slf4j
public abstract class AbstractHistoricalExchangeStrategy implements HistoricalExchangeStrategy {

    private final Exchange exchange;
    private final WebClient webClient;

    protected AbstractHistoricalExchangeStrategy(Exchange exchange, WebClient webClient) {
        this.exchange = exchange;
        this.webClient = webClient;
    }

    protected abstract Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor);

    protected abstract URI getKlinesUri(String tradingPair);

    protected abstract URI resolveKlinesUri(KlinesRequestContext context);

    protected abstract Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context);

    protected abstract URI resolveTickerUri(String tradingPair);

    protected abstract Mono<Ticker24hDto> executeWebClientTickerRequest(URI uri, TradingPair pair);

    protected abstract String getExchangeInterval(ChartInterval chartInterval);

    protected abstract String getTradingPairValue(TradingPair tradingPair);

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalData(HistoricalPriceRequest request) {
        return Mono.defer(() -> {
            String resolvedTradingPair = getTradingPairValue(request.getTradingPair());
            String intervalCode = getExchangeInterval(request.getInterval());
            Instant endTimeCursor = request.resolveEndTimeCursor();
            Instant startTimeCursor = calculateStartTimeCursor(request, endTimeCursor);

            KlinesRequestContext requestContext = KlinesRequestContext.builder()
                    .uri(getKlinesUri(resolvedTradingPair))
                    .tradingPair(resolvedTradingPair)
                    .intervalCode(intervalCode)
                    .endTimeCursor(endTimeCursor)
                    .startTimeCursor(startTimeCursor)
                    .originalRequest(request)
                    .build();

            URI uri = resolveKlinesUri(requestContext);
            return executeFetch(uri, requestContext);
        });
    }

    @Override
    public Mono<Ticker24hDto> fetch24hTicker(TradingPair pair) {
        String resolvedTradingPair = getTradingPairValue(pair);
        URI uri = resolveTickerUri(resolvedTradingPair);

        return executeWebClientTickerRequest(uri, pair);
    }

    protected <T, R> Mono<R> executeFetch(URI uri, ParameterizedTypeReference<T> reference, Function<T, R> mapper) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(reference)
                .map(mapper)
                .doOnError(error -> log.warn("[{}] Failed to fetch or parse ticker from {}. Error: {}",
                        exchange.name(), uri, error.getMessage()));
    }

    protected <T, R> Mono<R> executeFetch(URI uri, Class<T> clazz, Function<T, R> mapper) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(clazz)
                .map(mapper)
                .doOnError(error -> log.warn("[{}] Failed to fetch or parse ticker from {}. Error: {}",
                        exchange.name(), uri, error.getMessage()));
    }

    protected void validateExchangeInterval(ChartInterval chartInterval, String intervalCode) {
        if (intervalCode == null) {
            throw new UnsupportedIntervalException("Exchange '%s' does not support timeframe '%s'"
                    .formatted(exchange.name(), chartInterval));
        }
    }
}
