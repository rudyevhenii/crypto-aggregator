package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy.ConditionEvaluatorStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ConditionTypeConfig {

    @Bean
    public Map<ConditionType, ConditionEvaluatorStrategy> conditionEvaluatorStrategies(
            List<ConditionEvaluatorStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toMap(ConditionEvaluatorStrategy::getConditionType, Function.identity()));
    }
}
