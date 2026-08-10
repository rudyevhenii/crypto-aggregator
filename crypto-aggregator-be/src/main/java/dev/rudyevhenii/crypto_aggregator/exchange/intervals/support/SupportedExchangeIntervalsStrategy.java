package dev.rudyevhenii.crypto_aggregator.exchange.intervals.support;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;

public interface SupportedExchangeIntervalsStrategy {

    boolean isSupportedInterval(ChartInterval chartInterval);

    Exchange getExchangeType();
}
