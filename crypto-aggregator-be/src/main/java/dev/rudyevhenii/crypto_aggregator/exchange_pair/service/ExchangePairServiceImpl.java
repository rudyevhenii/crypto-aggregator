package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper.ExchangePairEntityMapper;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.EXCHANGE_PAIR_CACHE;
import static dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity.Fields;
import static dev.rudyevhenii.crypto_aggregator.exchange_pair.spec.ExchangePairSpec.equalToExchange;
import static dev.rudyevhenii.crypto_aggregator.exchange_pair.spec.ExchangePairSpec.hasTradingPairPattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangePairServiceImpl implements ExchangePairService {

    private final ExchangePairRepository repository;
    private final ExchangePairEntityMapper mapper;

    @Override
    @Cacheable(value = EXCHANGE_PAIR_CACHE)
    @Transactional(readOnly = true)
    public List<ExchangePair> findAllTradingPairs() {
        log.info("Finding all trading pairs");
        return repository.findAllByOrderByTradingPairAscExchange().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> searchByPattern(Exchange exchange, String tradingPair) {
        log.debug("Searching for trading pairs by pattern {}", tradingPair);
        Specification<ExchangePairEntity> spec = Specification
                .where(equalToExchange(exchange))
                .and(hasTradingPairPattern(tradingPair));
        Sort sort = Sort.by(Fields.tradingPair).and(Sort.by(Fields.exchange));

        return repository.findAll(spec, sort).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
