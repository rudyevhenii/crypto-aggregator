package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CoinbaseResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CoinbaseExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;

    public CoinbaseExchangeStrategy(@Qualifier("coinbaseWebClient") WebClient webClient) {
        super(webClient, EXCHANGE_NAME);
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return executeFetch("/v2/prices/BTC-USD/spot",
                CoinbaseResponse.class, this::toCryptoPriceBuilder);
    }

    private CryptoPriceDto toCryptoPriceBuilder(CoinbaseResponse res) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .symbol(Symbol.BTCUSDT)
                .price(res.data().price())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
