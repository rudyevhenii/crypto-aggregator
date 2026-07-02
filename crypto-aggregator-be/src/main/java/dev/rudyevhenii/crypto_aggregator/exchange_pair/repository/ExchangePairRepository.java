package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;

import java.util.List;
import java.util.UUID;

public interface ExchangePairRepository {

    List<ExchangePair> findAllTradingPairs();

    List<ExchangePair> searchByPattern(String pattern);

    boolean existsById(UUID id);
}
