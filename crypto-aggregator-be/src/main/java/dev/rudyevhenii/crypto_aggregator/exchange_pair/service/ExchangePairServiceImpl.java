package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangePairServiceImpl implements ExchangePairService {

    private final ExchangePairRepository repository;

    @Override
    public ExchangePair getById(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Exchange pair does not exist with id: %s"
                        .formatted(id)));
    }

    @Override
    public List<ExchangePair> findAllTradingPairs() {
        return repository.findAllTradingPairs();
    }

    @Override
    public List<ExchangePair> searchByPattern(String pattern) {
        return repository.searchByPattern(pattern);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
