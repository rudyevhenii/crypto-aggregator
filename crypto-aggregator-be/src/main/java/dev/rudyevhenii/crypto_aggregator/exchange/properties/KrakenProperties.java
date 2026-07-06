package dev.rudyevhenii.crypto_aggregator.exchange.properties;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "crypto.exchanges.kraken")
public record KrakenProperties(
        Map<TradingPair, String> tradingPair,
        Map<ChartInterval, String> chartInterval,
        String baseUrl
) {
}
