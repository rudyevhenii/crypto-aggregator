package dev.rudyevhenii.crypto_aggregator.properties;

import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "crypto")
public record CryptoProperties(
        Map<Exchange, ExchangeProperties> exchanges
) {
    public record ExchangeProperties(
            Map<TradingPair, String> symbols,
            Map<ChartInterval, String> chartInterval
    ) {
    }
}
