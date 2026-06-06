package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CoinbaseResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CoinbaseProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CoinbaseExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final String BASE_URI = "/v2/prices/%s/spot";

    private final CoinbaseProperties properties;

    public CoinbaseExchangeStrategy(@Qualifier("coinbaseWebClient") WebClient webClient,
                                    CoinbaseProperties properties) {
        super(webClient, EXCHANGE_NAME);
        this.properties = properties;
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        String symbol = properties.symbols().get(tradingPair);

        return executeFetch(BASE_URI.formatted(symbol),
                CoinbaseResponse.class,
                response -> toCryptoPriceBuilder(response, tradingPair));
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
