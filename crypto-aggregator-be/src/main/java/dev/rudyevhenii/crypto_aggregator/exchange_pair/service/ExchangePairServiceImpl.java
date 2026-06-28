package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangePairServiceImpl implements ExchangePairService {

    private final ExchangePairRepository repository;

    @Override
    public List<ExchangePair> findAllTradingPairs() {
        return repository.findAllTradingPairs();
    }

    @Override
    public List<ExchangePair> searchByPattern(String pattern) {
        return repository.searchByPattern(pattern);
    }
}
