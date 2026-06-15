package dev.rudyevhenii.crypto_aggregator.service.strategy.kraken;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenOhlcResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.mapper.KrakenTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.properties.KrakenProperties;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KrakenHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;
    private static final ParameterizedTypeReference<List<KrakenTicker24hResponse>> TICKER_RESPONSE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private static final URI KLINES_URI = URI.create("/0/public/OHLC");
    private static final URI TICKER_24H_URI = URI.create("/0/public/Ticker");

    private final KrakenProperties properties;
    private final KrakenTickerMapper mapper;

    public KrakenHistoricalExchangeStrategy(@Qualifier("krakenWebClient") WebClient webClient,
                                            KrakenProperties properties, KrakenTickerMapper mapper) {
        super(EXCHANGE_TYPE, webClient);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected Instant calculateStartTimeCursor(HistoricalPriceRequest request, Instant endTimeCursor) {
        Duration intervalDuration = request.getInterval().getDuration();
        long startTimeCursor = endTimeCursor.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());
        return Instant.ofEpochSecond(startTimeCursor);
    }

    @Override
    protected URI getKlinesUri(String tradingPair) {
        return KLINES_URI;
    }

    @Override
    protected URI resolveKlinesUri(KlinesRequestContext context) {
        return UriComponentsBuilder.fromUri(context.uri())
                .queryParam("pair", context.tradingPair())
                .queryParam("interval", context.intervalCode())
                .queryParam("since", context.startTimeCursor().getEpochSecond())
                .build()
                .toUri();
    }

    @Override
    protected Mono<List<HistoricalPriceDto>> executeFetch(URI uri, KlinesRequestContext context) {
        return executeFetch(uri, KrakenOhlcResponse.class,
                res -> mapper.toHistoricalPriceDto(res, context.endTimeCursor()));
    }

    @Override
    protected URI resolveTickerUri(String tradingPair) {
        return UriComponentsBuilder.fromUri(TICKER_24H_URI)
                .queryParam("pair", tradingPair)
                .build()
                .toUri();
    }

    @Override
    protected Mono<Ticker24hDto> executeWebClientTickerRequest(URI uri, TradingPair pair) {
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
    public Mono<List<Ticker24hDto>> fetch24hTickers() {
        List<TradingPair> tradingPairs = properties.tradingPair().keySet().stream().toList();

        String pairsParam = formatQueryParams(tradingPairs);
        URI uri = resolveTickerUri(pairsParam);

        return executeFetch(uri, TICKER_RESPONSE_REFERENCE, this::toTicker24h);
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }

    private List<Ticker24hDto> toTicker24h(List<KrakenTicker24hResponse> res) {
        return res.stream()
                .map(mapper::toTickerDto)
                .toList();
    }

    private String formatQueryParams(List<TradingPair> pairs) {
        return properties.tradingPair().entrySet().stream()
                .filter(entry -> pairs.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining(","));
    }
}
