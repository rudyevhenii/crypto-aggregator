package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.KrakenOhlcResponse;
import dev.rudyevhenii.crypto_aggregator.dto.KrakenResponse;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KrakenExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.KRAKEN;

    private static final String TICKER_URI = "/0/public/Ticker?pair=%s";
    private static final String KLINES_URI = "/0/public/OHLC?pair=%s&interval=%s&since=%d";

    private final CryptoProperties properties;

    public KrakenExchangeStrategy(@Qualifier("krakenWebClient") WebClient webClient,
                                  CryptoProperties properties) {
        super(webClient, EXCHANGE_NAME);
        this.properties = properties;
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        String symbol = getTradingPairCode(tradingPair);

        return executeFetch(TICKER_URI.formatted(symbol),
                KrakenResponse.class,
                response -> toCryptoPriceBuilder(response, tradingPair));
    }

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalPrices(HistoricalPriceRequest request) {
        String symbol = getTradingPairCode(request.getTradingPair());
        String intervalInMinutes = getAndValidateExchangeInterval(request.getInterval());

        Instant effectiveEntTime = request.getCursor() == null
                ? Instant.now()
                : request.getCursor().minusMillis(1);

        Duration intervalDuration = request.getInterval().getDuration();
        long startTime = effectiveEntTime.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());

        return executeHistoricalFetch(KLINES_URI.formatted(symbol, intervalInMinutes, startTime),
                KrakenOhlcResponse.class,
                response -> toKrakenKlines(response, effectiveEntTime));
    }

    @Override
    public CryptoProperties getCryptoProperties() {
        return properties;
    }

    private List<HistoricalPriceDto> toKrakenKlines(KrakenOhlcResponse response, Instant cursor) {
        JsonNode resultNode = response.result();
        JsonNode klinesArray = null;

        for (Map.Entry<String, JsonNode> field : resultNode.properties()) {
            if (!field.getKey().equals("last")) {
                klinesArray = field.getValue();
            }
        }

        if (klinesArray == null || !klinesArray.isArray()) {
            return List.of();
        }

        List<HistoricalPriceDto> klines = new ArrayList<>();
        for (JsonNode kline : klinesArray) {
            long timeInSeconds = kline.get(0).asLong();
            Instant openTime = Instant.ofEpochSecond(timeInSeconds);

            if (openTime.isAfter(cursor)) {
                continue;
            }
            klines.add(HistoricalPriceDto.builder()
                    .openTime(openTime)
                    .open(new BigDecimal(kline.get(1).asString()))
                    .high(new BigDecimal(kline.get(2).asString()))
                    .low(new BigDecimal(kline.get(3).asString()))
                    .close(new BigDecimal(kline.get(4).asString()))
                    .volume(new BigDecimal(kline.get(6).asString()))
                    .build());
        }
        return klines;
    }

    private CryptoPriceDto toCryptoPriceBuilder(KrakenResponse res, TradingPair tradingPair) {
        KrakenResponse.KrakenTicker ticker = res.result().values().iterator().next();
        BigDecimal price = new BigDecimal(ticker.c().get(0));

        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(price)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
