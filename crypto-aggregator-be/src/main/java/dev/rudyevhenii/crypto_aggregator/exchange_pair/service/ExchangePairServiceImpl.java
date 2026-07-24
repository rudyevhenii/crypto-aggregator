package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
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

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> findAllTradingPairs() {
        log.info("Finding all trading pairs");
        return repository.findAllTradingPairs();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangePair> searchByPattern(Exchange exchange, String tradingPair) {
        log.debug("Searching for trading pairs by pattern {}", tradingPair);
        return repository.searchByPattern(exchange, tradingPair);
    }
}
