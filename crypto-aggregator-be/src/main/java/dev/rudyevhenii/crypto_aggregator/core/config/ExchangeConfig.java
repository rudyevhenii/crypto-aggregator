package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.strategy.HistoricalExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.SupportedExchangeIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.live.strategy.LiveExchangeStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ExchangeConfig {

    @Bean
    public Map<Exchange, HistoricalExchangeStrategy> historicalExchangeStrategies(List<HistoricalExchangeStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toMap(HistoricalExchangeStrategy::getExchangeType, Function.identity()));
    }

    @Bean
    public Map<Exchange, LiveExchangeStrategy> liveExchangeStrategies(List<LiveExchangeStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toMap(LiveExchangeStrategy::getExchangeType, Function.identity()));
    }

    @Bean
    public Map<Exchange, SupportedExchangeIntervalsStrategy> supportedExchangeIntervals(
            List<SupportedExchangeIntervalsStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toMap(SupportedExchangeIntervalsStrategy::getExchangeType, Function.identity()));
    }
}
