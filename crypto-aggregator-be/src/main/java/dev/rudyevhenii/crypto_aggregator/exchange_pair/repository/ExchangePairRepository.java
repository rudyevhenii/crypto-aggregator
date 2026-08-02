package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;

import java.util.List;

public interface ExchangePairRepository {

    List<ExchangePair> findAllExchangePairs();

    List<ExchangePair> searchByPattern(Exchange exchange, String tradingPair);
}
