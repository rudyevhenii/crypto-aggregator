package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto.CoinbaseTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.mapper.CoinbaseTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.properties.CoinbaseProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractHistoricalExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.service.strategy.model.KlinesRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class CoinbaseHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.COINBASE;
    private static final ParameterizedTypeReference<List<List<Number>>> KLINES_RESPONSE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private static final String KLINES_URI = "/products/{product_id}/candles";
    private static final String TICKER_24H_URI = "/products/{product_id}/stats";

    private final CoinbaseProperties properties;
    private final CoinbaseTickerMapper mapper;

    public CoinbaseHistoricalExchangeStrategy(@Qualifier("coinbaseWebClient") WebClient webClient,
                                              CoinbaseProperties properties, CoinbaseTickerMapper mapper) {
        super(EXCHANGE_TYPE, webClient);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor) {
        Duration intervalDuration = request.getChartInterval().getDuration();
        long startTimeCursor = endTimeCursor.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());
        return Instant.ofEpochSecond(startTimeCursor);
    }

    @Override
    protected URI getKlinesUri(String resolvedTradingPair) {
        return UriComponentsBuilder.fromUriString(KLINES_URI)
                .build(resolvedTradingPair);
    }

    @Override
    protected URI resolveKlinesUri(KlinesRequestContext context) {
        return UriComponentsBuilder.fromUri(URI.create("https://api.exchange.coinbase.com").resolve(context.uri()))
                .queryParam("granularity", Long.parseLong(context.intervalCode()))
                .queryParam("start", context.startTimeCursor())
                .queryParam("end", context.endTimeCursor())
                .build()
                .toUri();
    }

    @Override
    protected Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context) {
        return executeFetch(uri, KLINES_RESPONSE_REFERENCE, mapper::toHistoricalPriceDto);
    }

    @Override
    protected URI resolveTickerUri(String tradingPair) {
        return UriComponentsBuilder.fromUriString(TICKER_24H_URI)
                .build(tradingPair);
    }

    @Override
    protected Mono<Ticker24hDto> executeWebClientTickerRequest(URI uri, TradingPair pair) {
        return executeFetch(uri, CoinbaseTicker24hResponse.class,
                res -> mapper.toTickerDto(res, pair));
    }

    @Override
    protected String getExchangeInterval(ChartInterval chartInterval) {
        String intervalCode = properties.chartInterval().get(chartInterval);
        validateExchangeInterval(chartInterval, intervalCode);
        return intervalCode;
    }

    @Override
    protected String getTradingPairValue(TradingPair tradingPair) {
        return properties.tradingPair().get(tradingPair);
    }

    @Override
    public Mono<List<Ticker24hDto>> fetch24hTickers() {
        List<TradingPair> tradingPairs = properties.tradingPair().keySet().stream().toList();
        return Flux.fromIterable(tradingPairs)
                .flatMap(this::fetch24hTicker)
                .collectList();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
