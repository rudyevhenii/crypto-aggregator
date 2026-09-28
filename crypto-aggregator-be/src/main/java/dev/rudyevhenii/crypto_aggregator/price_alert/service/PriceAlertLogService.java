package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;

import java.math.BigDecimal;
import java.util.List;

public interface PriceAlertLogService {

    void create(PriceAlert priceAlert, BigDecimal triggeredPrice);

    List<PriceAlertLog> getAllAlertLogs(PriceAlertLogScrollRequest request);
}
