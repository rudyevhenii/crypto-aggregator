package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.KrakenResponse;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class KrakenExchangeStrategy implements CryptoExchangeStrategy {

    private static final String KRAKEN_URL = "https://api.kraken.com";
    private final WebClient webClient;

    public KrakenExchangeStrategy(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(KRAKEN_URL)
                .build();
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return webClient.get()
                .uri("/0/public/Ticker?pair=XBTUSD")
                .retrieve()
                .bodyToMono(KrakenResponse.class)
                .log()
                .map(this::toCryptoPriceBuilder);
    }

    private CryptoPriceDto toCryptoPriceBuilder(KrakenResponse res) {
        KrakenResponse.KrakenTicker ticker = res.result().get("XXBTZUSD");
        BigDecimal price = new BigDecimal(ticker.c().get(0));

        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .symbol(Symbol.BTCUSDT)
                .price(price)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return Exchange.KRAKEN;
    }
}
