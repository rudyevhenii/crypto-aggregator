package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper.ExchangePairEntityMapper;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangePairServiceImpl implements ExchangePairService {

    private final ExchangePairRepository repository;
    private final ExchangePairEntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> findAllTradingPairs() {
        log.info("Finding all trading pairs");
        return repository.findAllByOrderByTradingPairAscExchange().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> searchByPattern(String pattern) {
        log.info("Searching for pattern '{}'", pattern);
        return repository.searchByPattern(pattern).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
