package dev.rudyevhenii.crypto_aggregator.config;

import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {CryptoProperties.class})
public class TradingPairConfig {

}
