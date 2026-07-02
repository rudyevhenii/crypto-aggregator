package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangePairServiceImpl implements ExchangePairService {

    private final ExchangePairRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> findAllTradingPairs() {
        return repository.findAllTradingPairs();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> searchByPattern(String pattern) {
        return repository.searchByPattern(pattern);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
