package dev.rudyevhenii.crypto_aggregator.exchange;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;

public interface ExchangeStrategy {

    Exchange getExchangeType();
}
