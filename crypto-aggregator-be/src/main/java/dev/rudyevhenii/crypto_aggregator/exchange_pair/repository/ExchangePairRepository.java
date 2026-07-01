package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangePairRepository {

    Optional<ExchangePair> findById(UUID id);

    List<ExchangePair> findAllTradingPairs();

    List<ExchangePair> searchByPattern(String pattern);

    boolean existsById(UUID id);
}
