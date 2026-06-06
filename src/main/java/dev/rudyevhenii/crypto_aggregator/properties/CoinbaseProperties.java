package dev.rudyevhenii.crypto_aggregator.properties;

import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "crypto.exchanges.coinbase")
public record CoinbaseProperties(Map<TradingPair, String> symbols) {
}
