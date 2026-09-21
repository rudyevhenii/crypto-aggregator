package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface PriceAlertService {

    PriceAlert create(PriceAlertRequest request);

    PriceAlert update(UUID id, PriceAlertUpdateRequest request);

    PriceAlert getPriceAlertById(UUID id);

    List<PriceAlert> getAllPriceAlerts();

    List<PriceAlert> getAllActive();

    void activate(UUID id);

    void deactivate(UUID id);

    void deactivateForUser(UUID userId, UUID id);

    void deleteById(UUID id);
}
