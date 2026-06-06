package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CoinbaseResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CoinbaseExchangeStrategy implements CryptoExchangeStrategy {

    private static final String COINBASE_URL = "https://api.coinbase.com";
    private final WebClient webClient;

    public CoinbaseExchangeStrategy(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(COINBASE_URL)
                .build();
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return webClient.get()
                .uri("/v2/prices/BTC-USD/spot")
                .retrieve()
                .bodyToMono(CoinbaseResponse.class)
                .log()
                .map(this::toCryptoPriceBuilder);
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
        return Exchange.COINBASE;
    }
}
