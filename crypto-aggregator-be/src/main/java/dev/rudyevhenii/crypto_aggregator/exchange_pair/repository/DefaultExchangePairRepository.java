package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper.ExchangePairEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultExchangePairRepository implements ExchangePairRepository {

    private final SpringDataExchangePairRepository repository;
    private final ExchangePairEntityMapper mapper;

    @Override
    public List<ExchangePair> findAllTradingPairs() {
        return repository.findAllByOrderByTradingPairAscExchange().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExchangePair> searchByPattern(String pattern) {
        return repository.searchByPattern(pattern).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
