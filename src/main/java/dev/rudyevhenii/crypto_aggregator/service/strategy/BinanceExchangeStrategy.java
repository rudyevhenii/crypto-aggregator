package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.BinanceResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class BinanceExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.BINANCE;

    public BinanceExchangeStrategy(@Qualifier("binanceWebClient") WebClient webClient) {
        super(webClient, EXCHANGE_NAME);
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return executeFetch("/api/v3/ticker/price?symbol=BTCUSDT",
                BinanceResponse.class, this::toCryptoPriceBuilder);
    }

    private CryptoPriceDto toCryptoPriceBuilder(BinanceResponse res) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .symbol(Symbol.BTCUSDT)
                .price(res.price())
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
