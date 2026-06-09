package dev.rudyevhenii.crypto_aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient binanceWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.binance.com").build();
    }

    @Bean
    public WebClient coinbaseRetailWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.coinbase.com").build();
    }

    @Bean
    public WebClient coinbaseExchangeWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.exchange.coinbase.com").build();
    }

    @Bean
    public WebClient krakenWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.kraken.com").build();
    }
}
