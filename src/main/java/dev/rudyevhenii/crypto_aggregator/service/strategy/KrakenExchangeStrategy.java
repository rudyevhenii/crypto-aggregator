package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.KrakenResponse;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class KrakenExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.KRAKEN;

    public KrakenExchangeStrategy(@Qualifier("krakenWebClient") WebClient webClient) {
        super(webClient, EXCHANGE_NAME);
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice() {
        return executeFetch("/0/public/Ticker?pair=XBTUSD",
                KrakenResponse.class, this::toCryptoPriceBuilder);
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
        return EXCHANGE_NAME;
    }
}
