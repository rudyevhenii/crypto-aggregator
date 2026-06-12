package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;

public interface ExchangeStrategy {

    CryptoProperties getProperties();

    Exchange getExchangeType();
}
