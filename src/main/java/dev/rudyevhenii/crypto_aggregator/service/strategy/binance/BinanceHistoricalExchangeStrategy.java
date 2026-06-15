package dev.rudyevhenii.crypto_aggregator.service.strategy.binance;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.binance.dto.BinanceTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.binance.mapper.BinanceTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.binance.properties.BinanceProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractHistoricalExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.service.strategy.model.KlinesRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.BINANCE;
    private static final ParameterizedTypeReference<List<List<Number>>> KLINES_RESPONSE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<BinanceTicker24hResponse>> TICKER_RESPONSE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private static final URI KLINES_URI = URI.create("/api/v3/klines");
    private static final URI TICKER_24H_URI = URI.create("/api/v3/ticker/24h");

    private final BinanceProperties properties;
    private final BinanceTickerMapper mapper;

    public BinanceHistoricalExchangeStrategy(@Qualifier("binanceWebClient") WebClient webClient,
                                             BinanceProperties properties, BinanceTickerMapper mapper) {
        super(EXCHANGE_TYPE, webClient);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor) {
        return endTimeCursor;
    }

    @Override
    protected URI getKlinesUri(String resolvedTradingPair) {
        return KLINES_URI;
    }

    @Override
    protected URI resolveKlinesUri(KlinesRequestContext context) {
        return UriComponentsBuilder.fromUri(context.uri())
                .queryParam("symbol", context.tradingPair())
                .queryParam("interval", context.intervalCode())
                .queryParam("endTime", context.endTimeCursor().toEpochMilli())
                .queryParam("limit", context.originalRequest().getLimit())
                .build()
                .toUri();
    }

    @Override
    protected Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context) {
        return executeFetch(uri, KLINES_RESPONSE_REFERENCE, mapper::toHistoricalPriceDto);
    }

    @Override
    protected URI resolveTickerUri(String tradingPair) {
        return UriComponentsBuilder.fromUri(TICKER_24H_URI)
                .queryParam("symbol", tradingPair)
                .build()
                .toUri();
    }

    @Override
    protected Mono<Ticker24hDto> executeWebClientTickerRequest(URI uri, TradingPair pair) {
        return executeFetch(uri, BinanceTicker24hResponse.class, mapper::toTickerDto);
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
    public Mono<List<Ticker24hDto>> fetch24hTickers(List<TradingPair> pairs) {
        String pairsParam = formatQueryParams(pairs);
        URI uri = resolveTickerUri(pairsParam);

        return executeFetch(uri, TICKER_RESPONSE_REFERENCE, this::toTicker24h);
    }

    private List<Ticker24hDto> toTicker24h(List<BinanceTicker24hResponse> res) {
        return res.stream()
                .map(mapper::toTickerDto)
                .toList();
    }

    private String formatQueryParams(List<TradingPair> pairs) {
        return properties.tradingPair().entrySet().stream()
                .filter(entry -> pairs.contains(entry.getKey()))
                .map(entry -> "\"%s\"".formatted(entry.getValue()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
