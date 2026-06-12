package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exception.UnsupportedIntervalException;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.model.KlinesRequestContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Slf4j
public abstract class AbstractHistoricalExchangeStrategy implements HistoricalExchangeStrategy {

    private final Exchange exchange;
    private final CryptoProperties properties;

    protected AbstractHistoricalExchangeStrategy(Exchange exchange, CryptoProperties properties) {
        this.exchange = exchange;
        this.properties = properties;
    }

    protected abstract Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor);

    protected abstract URI getKlinesUri(String tradingPair);

    protected abstract URI resolveKlinesUri(KlinesRequestContext context);

    protected abstract Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context);

    @Override
    public CryptoProperties getProperties() {
        return properties;
    }

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalData(HistoricalPriceRequest request) {
        String resolvedTradingPair = getTradingPairCode(request.getTradingPair());
        String intervalCode = getExchangeInterval(request.getInterval());
        Instant endTimeCursor = resolveEndTimeCursor(request);
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
    }

    private static Instant resolveEndTimeCursor(HistoricalPriceRequest request) {
        return request.getCursor() == null
                ? Instant.now()
                : request.getCursor().minusMillis(1);
    }

    private String getExchangeInterval(ChartInterval chartInterval) {
        String intervalCode = getExchangeProperties().chartInterval().get(chartInterval);
        if (intervalCode == null) {
            throw new UnsupportedIntervalException("Exchange '%s' does not support timeframe '%s'".formatted(
                    exchange.name(), chartInterval));
        }
        return intervalCode;
    }

    private String getTradingPairCode(TradingPair tradingPair) {
        return getExchangeProperties().tradingPair().get(tradingPair);
    }

    private CryptoProperties.ExchangeProperties getExchangeProperties() {
        return getProperties().exchanges().get(getExchangeType());
    }
}
