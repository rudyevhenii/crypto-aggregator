package dev.rudyevhenii.crypto_aggregator.exchange.intervals.support;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BinanceSupportedIntervalsStrategy implements SupportedExchangeIntervalsStrategy {

    private static final Set<ChartInterval> SUPPORTED_CHART_INTERVALS;

    static {
        SUPPORTED_CHART_INTERVALS = Set.of(
                ChartInterval.ONE_SECOND,
                ChartInterval.ONE_MINUTE,
                ChartInterval.THREE_MINUTES,
                ChartInterval.FIVE_MINUTES,
                ChartInterval.FIFTEEN_MINUTES,
                ChartInterval.THIRTY_MINUTES,
                ChartInterval.ONE_HOUR,
                ChartInterval.TWO_HOURS,
                ChartInterval.FOUR_HOURS,
                ChartInterval.SIX_HOURS,
                ChartInterval.EIGHT_HOURS,
                ChartInterval.TWELVE_HOURS,
                ChartInterval.ONE_DAY,
                ChartInterval.THREE_DAYS,
                ChartInterval.ONE_WEEK,
                ChartInterval.ONE_MONTH
        );
    }

    @Override
    public boolean isSupportedInterval(ChartInterval chartInterval) {
        return SUPPORTED_CHART_INTERVALS.contains(chartInterval);
    }

    @Override
    public Exchange getExchangeType() {
        return Exchange.BINANCE;
    }
}
