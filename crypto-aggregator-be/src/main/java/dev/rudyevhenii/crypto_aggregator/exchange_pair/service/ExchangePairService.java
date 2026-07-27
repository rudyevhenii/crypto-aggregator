package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;

import java.util.List;

public interface ExchangePairService {

    List<ExchangePair> findAllExchangePairs();

    List<ExchangePair> searchByPattern(Exchange exchange, String tradingPair);
}
