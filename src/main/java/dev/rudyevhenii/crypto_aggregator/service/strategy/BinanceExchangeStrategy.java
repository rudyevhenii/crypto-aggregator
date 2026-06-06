package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.BinanceResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.BinanceProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class BinanceExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.BINANCE;
    private static final String BASE_URI = "/api/v3/ticker/price?symbol=%s";

    private final BinanceProperties properties;

    public BinanceExchangeStrategy(@Qualifier("binanceWebClient") WebClient webClient,
                                   BinanceProperties properties) {
        super(webClient, EXCHANGE_NAME);
        this.properties = properties;
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        String symbol = properties.symbols().get(tradingPair);

        return executeFetch(BASE_URI.formatted(symbol),
                BinanceResponse.class,
                response -> toCryptoPriceBuilder(response, tradingPair));
    }

    private CryptoPriceDto toCryptoPriceBuilder(BinanceResponse res, TradingPair tradingPair) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(res.price())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
