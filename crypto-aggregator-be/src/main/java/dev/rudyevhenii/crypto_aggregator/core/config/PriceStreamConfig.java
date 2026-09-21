package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class PriceStreamConfig {

    @Bean
    public Sinks.Many<LivePriceDto> priceSink() {
        return Sinks.many().multicast().directBestEffort();
    }
}
