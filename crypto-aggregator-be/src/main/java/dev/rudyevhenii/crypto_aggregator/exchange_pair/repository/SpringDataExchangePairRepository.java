package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataExchangePairRepository extends JpaRepository<ExchangePairEntity, UUID> {

    List<ExchangePairEntity> findAllByOrderByTradingPairAscExchange();

    @Query("""
              SELECT e FROM ExchangePairEntity e
              WHERE CAST(e.tradingPair AS STRING) ILIKE CONCAT('%', :pattern, '%')
              OR CAST(e.exchange AS STRING) ILIKE CONCAT('%', :pattern, '%')
              ORDER BY e.tradingPair, e.exchange
            """)
    List<ExchangePairEntity> searchByPattern(@Param("pattern") String pattern);
}
