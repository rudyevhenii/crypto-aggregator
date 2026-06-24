package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.exchange.properties.BinanceProperties;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.CoinbaseProperties;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.KrakenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {BinanceProperties.class, CoinbaseProperties.class, KrakenProperties.class})
public class TradingPairConfig {

}
