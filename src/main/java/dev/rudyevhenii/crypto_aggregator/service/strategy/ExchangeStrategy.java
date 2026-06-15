package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;

public interface ExchangeStrategy {

    Exchange getExchangeType();
}
