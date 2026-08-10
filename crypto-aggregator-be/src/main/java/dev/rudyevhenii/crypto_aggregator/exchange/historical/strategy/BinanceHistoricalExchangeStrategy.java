package dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.context.KlinesRequestContext;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.BinanceTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper.HistoricalBinanceMapper;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.BinanceSupportedIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.BinanceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.BINANCE;
    private static final ParameterizedTypeReference<List<List<Number>>> KLINES_REF
            = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<BinanceTicker24hResponse>> TICKER_REF
            = new ParameterizedTypeReference<>() {
    };

    private static final URI KLINES_URI = URI.create("/api/v3/klines");
    private static final URI TICKER_24H_URI = URI.create("/api/v3/ticker/24hr");

    private final BinanceProperties properties;
    private final HistoricalBinanceMapper mapper;
    private final BinanceSupportedIntervalsStrategy supportedIntervals;

    public BinanceHistoricalExchangeStrategy(BinanceProperties properties, HistoricalBinanceMapper mapper,
                                             BinanceSupportedIntervalsStrategy supportedIntervals) {
        super(EXCHANGE_TYPE);
        this.properties = properties;
        this.mapper = mapper;
        this.supportedIntervals = supportedIntervals;
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
        return UriComponentsBuilder.fromUri(URI.create(properties.baseUrl()).resolve(context.uri()))
                .queryParam("symbol", context.tradingPair())
                .queryParam("interval", context.intervalCode())
                .queryParam("endTime", context.endTimeCursor().toEpochMilli())
                .queryParam("limit", context.originalRequest().getLimit())
                .build()
                .toUri();
    }

    @Override
    protected List<HistoricalPriceDto> executeFetch(URI uri, KlinesRequestContext context) {
        return executeFetch(uri, KLINES_REF, mapper::toHistoricalPriceDto);
    }

    @Override
    protected URI resolveTickerUri(String tradingPair) {
        return UriComponentsBuilder.fromUri(URI.create(properties.baseUrl()).resolve(TICKER_24H_URI))
                .queryParam("symbol", tradingPair)
                .build()
                .toUri();
    }

    @Override
    protected Ticker24hDto executeWebClientTickerRequest(URI uri, TradingPair pair) {
        return executeFetch(uri, BinanceTicker24hResponse.class, mapper::toTickerDto);
    }

    @Override
    protected String getExchangeInterval(ChartInterval chartInterval) {
        boolean isSupported = supportedIntervals.isSupportedInterval(chartInterval);
        validateExchangeInterval(isSupported, chartInterval);
        return properties.chartInterval().get(chartInterval);
    }

    @Override
    protected String getTradingPairValue(TradingPair tradingPair) {
        return properties.tradingPair().get(tradingPair);
    }

    @Override
    public List<Ticker24hDto> fetch24hTickers(List<TradingPair> tradingPairs) {
        String pairsParam = formatQueryParams(tradingPairs);
        URI uri = resolveTickerUriWithMultipleParameters(pairsParam);

        return executeFetch(uri, TICKER_REF, this::toTicker24h);
    }

    private URI resolveTickerUriWithMultipleParameters(String tradingPair) {
        return UriComponentsBuilder.fromUri(URI.create(properties.baseUrl()).resolve(TICKER_24H_URI))
                .queryParam("symbols", tradingPair)
                .build()
                .toUri();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }

    private List<Ticker24hDto> toTicker24h(List<BinanceTicker24hResponse> res) {
        return res.stream()
                .map(mapper::toTickerDto)
                .collect(Collectors.toList());
    }

    private String formatQueryParams(List<TradingPair> pairs) {
        return properties.tradingPair().entrySet().stream()
                .filter(entry -> pairs.contains(entry.getKey()))
                .map(entry -> "\"%s\"".formatted(entry.getValue()))
                .collect(Collectors.joining(",", "[", "]"));
    }
}
