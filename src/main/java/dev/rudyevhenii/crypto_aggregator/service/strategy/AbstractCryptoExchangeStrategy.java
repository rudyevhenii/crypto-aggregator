package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public abstract class AbstractCryptoExchangeStrategy implements CryptoExchangeStrategy {

    private final WebClient webClient;
    private final Exchange exchange;

    protected AbstractCryptoExchangeStrategy(WebClient webClient, Exchange exchange) {
        this.webClient = webClient;
        this.exchange = exchange;
    }

    protected <T> Mono<CryptoPriceDto> executeFetch(String uri, Class<T> responseClass,
                                                    Function<T, CryptoPriceDto> mapper) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseClass)
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        exchange.name(), error.getMessage()))
                .map(mapper);
    }
}
