package dev.rudyevhenii.crypto_aggregator.exchange.intervals.support;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.BinanceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceSupportedIntervalsStrategy implements SupportedExchangeIntervalsStrategy {

    private final BinanceProperties properties;

    @Override
    public boolean isSupportedInterval(ChartInterval chartInterval) {
        return properties.chartInterval().containsKey(chartInterval);
    }

    @Override
    public Exchange getExchangeType() {
        return Exchange.BINANCE;
    }
}
