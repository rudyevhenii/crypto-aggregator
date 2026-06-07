package dev.rudyevhenii.crypto_aggregator.config;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.service.strategy.CryptoExchangeStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ExchangeConfig {

    @Bean
    public Map<Exchange, CryptoExchangeStrategy> exchangeStrategies(List<CryptoExchangeStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toMap(CryptoExchangeStrategy::getExchangeType, Function.identity()));
    }
}
