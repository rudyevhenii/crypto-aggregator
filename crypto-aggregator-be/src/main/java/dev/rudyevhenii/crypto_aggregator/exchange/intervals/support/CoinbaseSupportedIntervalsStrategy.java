package dev.rudyevhenii.crypto_aggregator.exchange.intervals.support;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CoinbaseSupportedIntervalsStrategy implements SupportedExchangeIntervalsStrategy {

    private static final Set<ChartInterval> SUPPORTED_CHART_INTERVALS;

    static {
        SUPPORTED_CHART_INTERVALS = Set.of(
                ChartInterval.ONE_MINUTE,
                ChartInterval.FIVE_MINUTES,
                ChartInterval.FIFTEEN_MINUTES,
                ChartInterval.ONE_HOUR,
                ChartInterval.SIX_HOURS,
                ChartInterval.ONE_DAY
        );
    }

    @Override
    public boolean isSupportedInterval(ChartInterval chartInterval) {
        return SUPPORTED_CHART_INTERVALS.contains(chartInterval);
    }

    @Override
    public Exchange getExchangeType() {
        return Exchange.COINBASE;
    }
}
