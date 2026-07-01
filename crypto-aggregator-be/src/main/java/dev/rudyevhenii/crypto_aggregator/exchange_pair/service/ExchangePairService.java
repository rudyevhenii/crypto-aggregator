package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;

import java.util.List;
import java.util.UUID;

public interface ExchangePairService {

    ExchangePair getById(UUID id);

    List<ExchangePair> findAllTradingPairs();

    List<ExchangePair> searchByPattern(String pattern);

    boolean existsById(UUID id);
}
