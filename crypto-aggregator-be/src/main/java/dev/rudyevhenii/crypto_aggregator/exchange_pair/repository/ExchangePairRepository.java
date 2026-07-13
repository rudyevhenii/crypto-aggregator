package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ExchangePairRepository extends JpaRepository<ExchangePairEntity, UUID>,
        JpaSpecificationExecutor<ExchangePairEntity> {

    List<ExchangePairEntity> findAllByOrderByTradingPairAscExchange();
}
