package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPriceAlertRepository extends JpaRepository<PriceAlertEntity, UUID> {

    Optional<PriceAlertEntity> findByUserIdAndId(UUID userId, UUID id);

    List<PriceAlertEntity> findAllByUserIdOrderByActiveDesc(UUID userId);

    List<PriceAlertEntity> findAllByActiveIsTrue();

    @Query(value = """
            UPDATE "priceAlerts" SET active = false
            WHERE active = true
            AND "expiresAt" <= now() RETURNING *""", nativeQuery = true)
    List<PriceAlertEntity> deactivateExpiredAlerts();
}
