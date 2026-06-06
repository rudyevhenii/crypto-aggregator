package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.KrakenResponse;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.KrakenProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class KrakenExchangeStrategy extends AbstractCryptoExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.KRAKEN;
    private static final String BASE_URI = "/0/public/Ticker?pair=%s";

    private final KrakenProperties properties;

    public KrakenExchangeStrategy(@Qualifier("krakenWebClient") WebClient webClient,
                                  KrakenProperties properties) {
        super(webClient, EXCHANGE_NAME);
        this.properties = properties;
    }

    @Override
    public Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        String symbol = properties.symbols().get(tradingPair);

        return executeFetch(BASE_URI.formatted(symbol),
                KrakenResponse.class,
                response -> toCryptoPriceBuilder(response, tradingPair));
    }

    private CryptoPriceDto toCryptoPriceBuilder(KrakenResponse res, TradingPair tradingPair) {
        KrakenResponse.KrakenTicker ticker = res.result().values().iterator().next();
        BigDecimal price = new BigDecimal(ticker.c().get(0));

        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(price)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
