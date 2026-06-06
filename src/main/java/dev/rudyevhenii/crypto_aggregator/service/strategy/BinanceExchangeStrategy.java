package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.BinanceResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class BinanceExchangeStrategy implements CryptoExchangeStrategy {

    private static final String BINANCE_URL = "https://api.binance.com";
    private final WebClient webClient;

    public BinanceExchangeStrategy(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(BINANCE_URL)
                .build();
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return webClient.get()
                .uri("/api/v3/ticker/price?symbol=BTCUSDT")
                .retrieve()
                .bodyToMono(BinanceResponse.class)
                .log()
                .map(this::toCryptoPriceBuilder);
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
        return Exchange.BINANCE;
    }
}
