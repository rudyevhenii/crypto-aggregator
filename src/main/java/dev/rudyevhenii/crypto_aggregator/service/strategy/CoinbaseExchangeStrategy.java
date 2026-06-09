package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CoinbaseResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class CoinbaseExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final ParameterizedTypeReference<List<List<Number>>> PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private static final String TICKER_URI = "/v2/prices/%s/spot";
    private static final String KLINES_URI = "/products/%s/candles?granularity=%d&start=%s&end=%s";

    private final WebClient exchangeWebClient;
    private final CryptoProperties properties;

    public CoinbaseExchangeStrategy(@Qualifier("coinbaseRetailWebClient") WebClient retailWebClient,
                                    @Qualifier("coinbaseExchangeWebClient") WebClient exchangeWebClient,
                                    CryptoProperties properties) {
        super(retailWebClient, EXCHANGE_NAME);
        this.exchangeWebClient = exchangeWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        String symbol = getTradingPairCode(tradingPair);

        return executeFetch(TICKER_URI.formatted(symbol),
                CoinbaseResponse.class,
                response -> toCryptoPriceBuilder(response, tradingPair));
    }

    @Override
    public Mono<List<HistoricalPriceDto>> fetchHistoricalPrices(HistoricalPriceRequest request) {
        String symbol = getTradingPairCode(request.getTradingPair());
        long intervalInSeconds = Long.parseLong(getAndValidateExchangeInterval(request.getInterval()));

        Instant cursor = request.getCursor() == null
                ? Instant.now()
                : request.getCursor().minusMillis(1);

        Duration intervalDuration = request.getInterval().getDuration();
        long startTime = cursor.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());

        return exchangeWebClient.get()
                .uri(KLINES_URI.formatted(symbol, intervalInSeconds,
                        Instant.ofEpochSecond(startTime), cursor))
                .retrieve()
                .bodyToMono(PARAMETERIZED_TYPE_REFERENCE)
                .onErrorStop()
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        getExchangeType().name(), error.getMessage()))
                .map(this::toCoinbaseKlines);
    }

    @Override
    public CryptoProperties getCryptoProperties() {
        return properties;
    }

    private List<HistoricalPriceDto> toCoinbaseKlines(List<List<Number>> klines) {
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

    private CryptoPriceDto toCryptoPriceBuilder(CoinbaseResponse res, TradingPair tradingPair) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(res.data().price())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
