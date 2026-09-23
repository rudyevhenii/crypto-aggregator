package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpringDataPriceAlertLogRepository extends JpaRepository<PriceAlertLogEntity, UUID>,
        JpaSpecificationExecutor<PriceAlertLogEntity> {
}
