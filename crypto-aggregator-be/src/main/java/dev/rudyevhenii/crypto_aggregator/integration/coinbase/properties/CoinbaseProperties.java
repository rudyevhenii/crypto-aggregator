package dev.rudyevhenii.crypto_aggregator.integration.coinbase.properties;

import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "crypto.exchanges.coinbase")
public record CoinbaseProperties(
        Map<TradingPair, String> tradingPair,
        Map<ChartInterval, String> chartInterval
) {
}
