package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper.ExchangePairEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.EXCHANGE_PAIR_CACHE;
import static dev.rudyevhenii.crypto_aggregator.exchange_pair.spec.ExchangePairSpec.equalToExchange;
import static dev.rudyevhenii.crypto_aggregator.exchange_pair.spec.ExchangePairSpec.hasTradingPairPattern;

@Repository
@RequiredArgsConstructor
public class DefaultExchangePairRepository implements ExchangePairRepository {

    private final SpringDataExchangePairRepository repository;
    private final ExchangePairEntityMapper mapper;

    @Override
    @Cacheable(value = EXCHANGE_PAIR_CACHE)
    public List<ExchangePair> findAllExchangePairs() {
        return repository.findAllByOrderByTradingPairAscExchange().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExchangePair> searchByPattern(Exchange exchange, String tradingPair) {
        Specification<ExchangePairEntity> spec = Specification
                .where(equalToExchange(exchange))
                .and(hasTradingPairPattern(tradingPair));
        Sort sort = Sort.by(ExchangePairEntity.Fields.tradingPair).and(Sort.by(ExchangePairEntity.Fields.exchange));

        return repository.findAll(spec, sort).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = EXCHANGE_PAIR_CACHE, key = "#id", unless = "#result == null")
    public Optional<ExchangePair> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
