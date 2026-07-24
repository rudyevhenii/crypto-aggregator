package dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.context.KlinesRequestContext;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.KrakenOhlcResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.KrakenTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper.HistoricalKrakenMapper;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.KrakenProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KrakenHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;

    private static final URI KLINES_URI = URI.create("/0/public/OHLC");
    private static final URI TICKER_24H_URI = URI.create("/0/public/Ticker");

    private final KrakenProperties properties;
    private final HistoricalKrakenMapper mapper;

    public KrakenHistoricalExchangeStrategy(KrakenProperties properties, HistoricalKrakenMapper mapper) {
        super(EXCHANGE_TYPE);
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
    protected URI getKlinesUri(String tradingPair) {
        return KLINES_URI;
    }

    @Override
    protected URI resolveKlinesUri(KlinesRequestContext context) {
        return UriComponentsBuilder.fromUri(URI.create(properties.baseUrl()).resolve(context.uri()))
                .queryParam("pair", context.tradingPair())
                .queryParam("interval", context.intervalCode())
                .queryParam("since", context.startTimeCursor().getEpochSecond())
                .build()
                .toUri();
    }

    @Override
    protected List<HistoricalPriceDto> executeFetch(URI uri, KlinesRequestContext context) {
        return executeFetch(uri, KrakenOhlcResponse.class,
                res -> mapper.toHistoricalPriceDto(res, context.endTimeCursor()));
    }

    @Override
    protected URI resolveTickerUri(String tradingPair) {
        return UriComponentsBuilder.fromUri(URI.create(properties.baseUrl()).resolve(TICKER_24H_URI))
                .queryParam("pair", tradingPair)
                .build()
                .toUri();
    }

    @Override
    protected Ticker24hDto executeWebClientTickerRequest(URI uri, TradingPair pair) {
        return executeFetch(uri, KrakenTicker24hResponse.class, mapper::toTickerDto);
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
    public List<Ticker24hDto> fetch24hTickers(List<TradingPair> tradingPairs) {
        String pairsParam = formatQueryParams(tradingPairs);
        URI uri = resolveTickerUri(pairsParam);

        return executeFetch(uri, KrakenTicker24hResponse.class, mapper::toTickerDtoList);
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }

    private String formatQueryParams(List<TradingPair> pairs) {
        return properties.tradingPair().entrySet().stream()
                .filter(entry -> pairs.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining(","));
    }
}
