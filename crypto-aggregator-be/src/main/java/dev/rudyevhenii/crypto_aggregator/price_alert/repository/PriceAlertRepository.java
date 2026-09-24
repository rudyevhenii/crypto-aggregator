package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceAlertRepository {

    PriceAlert create(PriceAlert priceAlert);

    PriceAlert update(PriceAlert priceAlert);

    Optional<PriceAlert> findById(UUID userId, UUID id);

    List<PriceAlert> findAll(UUID userId);

    List<PriceAlert> findAllActive();

    List<PriceAlert> deactivateExpiredAlerts();

    void deleteById(UUID id);
}
