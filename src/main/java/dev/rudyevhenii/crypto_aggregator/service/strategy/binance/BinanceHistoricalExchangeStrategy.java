package dev.rudyevhenii.crypto_aggregator.service.strategy.binance;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractHistoricalExchangeStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component
public class BinanceHistoricalExchangeStrategy extends AbstractHistoricalExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.BINANCE;
    private static final ParameterizedTypeReference<List<List<Number>>> PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };
    private static final String KLINES_URI = "/api/v3/klines?symbol=%s&interval=%s&endTime=%d&limit=%d";

    private final CryptoProperties properties;

    public BinanceHistoricalExchangeStrategy(@Qualifier("binanceWebClient") WebClient webClient,
                                             CryptoProperties properties) {
        super(webClient, EXCHANGE_NAME);
        this.properties = properties;
    }

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalPrices(HistoricalPriceRequest request) {
        String symbol = getTradingPairCode(request.getTradingPair());
        String intervalCode = getAndValidateExchangeInterval(request.getInterval());

        Instant cursor = request.getCursor() == null
                ? Instant.now()
                : request.getCursor().minusMillis(1);

        long endTimeMillis = cursor.toEpochMilli();

        return executeHistoricalFetch(KLINES_URI.formatted(symbol, intervalCode, endTimeMillis, request.getLimit()),
                PARAMETERIZED_TYPE_REFERENCE,
                this::toBinanceKlines);
    }

    private List<HistoricalPriceDto> toBinanceKlines(List<List<Number>> klines) {
        if (klines == null || klines.isEmpty()) {
            return Collections.emptyList();
        }

        return klines.stream()
                .map(kline -> {
                    long timeInSeconds = kline.get(0).longValue();
                    Instant openTime = Instant.ofEpochMilli(timeInSeconds);

                    return HistoricalPriceDto.builder()
                            .openTime(openTime)
                            .open(new BigDecimal(kline.get(1).toString()))
                            .high(new BigDecimal(kline.get(2).toString()))
                            .low(new BigDecimal(kline.get(3).toString()))
                            .close(new BigDecimal(kline.get(4).toString()))
                            .volume(new BigDecimal(kline.get(5).toString()))
                            .build();
                })
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
