package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;

import java.util.List;
import java.util.UUID;

public interface PriceAlertLogRepository {

    void create(PriceAlertLog priceAlertLog);

    List<PriceAlertLog> findAllAlertLogs(UUID userId, PriceAlertLogScrollRequest request);
}
