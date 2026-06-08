package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exception.UnsupportedIntervalException;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
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
                .onErrorStop()
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        exchange.name(), error.getMessage()))
                .map(mapper);
    }

    protected <T> Mono<List<HistoricalPriceDto>> executeHistoricalFetch(String uri, Class<T> responseClass,
                                                                        Function<T, List<HistoricalPriceDto>> mapper) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorStop()
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        exchange.name(), error.getMessage()))
                .map(mapper);
    }

    protected <T> Mono<List<HistoricalPriceDto>> executeHistoricalFetch(String uri, ParameterizedTypeReference<T> responseClass,
                                                                        Function<T, List<HistoricalPriceDto>> mapper) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorStop()
                .doOnError(error -> log.warn("Exception occurred while fetching price from {}: {}",
                        exchange.name(), error.getMessage()))
                .map(mapper);
    }

    protected String getExchangeIntervalCode(ChartInterval chartInterval) {
        String intervalCode = getExchangeProperties().chartInterval().get(chartInterval);
        if (intervalCode == null) {
            throw new UnsupportedIntervalException("Exchange '%s' does not support timeframe '%s'".formatted(
                    exchange.name(), chartInterval));
        }
        return intervalCode;
    }

    private CryptoProperties.ExchangeProperties getExchangeProperties() {
        return getCryptoProperties().exchanges().get(getExchangeType());
    }

    protected String getTradingPairCode(TradingPair tradingPair) {
        return getExchangeProperties().symbols().get(tradingPair);
    }
}
