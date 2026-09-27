package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.PercentagePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TrailingPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAlertLogServiceImpl implements PriceAlertLogService {

    private final PriceAlertLogRepository repository;
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public void create(PriceAlert priceAlert, BigDecimal triggeredPrice) {
        PriceAlertLog priceAlertLog = toDomain(priceAlert, triggeredPrice);
        repository.create(priceAlertLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceAlertLog> getAllAlertLogs(PriceAlertLogScrollRequest request) {
        return repository.findAllAlertLogs(userContext.getUserId(), request);
    }

    private PriceAlertLog toDomain(PriceAlert priceAlert, BigDecimal triggeredPrice) {
        return PriceAlertLog.builder()
                .id(generator.uuid())
                .priceAlertId(priceAlert.getId())
                .userId(priceAlert.getUserId())
                .exchange(priceAlert.getExchange())
                .tradingPair(priceAlert.getTradingPair())
                .conditionType(priceAlert.getConditionPayload().getConditionType())
                .triggeredPrice(triggeredPrice)
                .message(buildMessage(priceAlert))
                .deliveryMethods(priceAlert.getDeliveryMethods())
                .createdAt(generator.now())
                .build();
    }

    private String buildMessage(PriceAlert priceAlert) {
        TradingPair tradingPair = priceAlert.getTradingPair();
        ConditionPayload conditionPayload = priceAlert.getConditionPayload();
        BigDecimal targetPrice = resolveTargetPrice(conditionPayload);

        return switch (conditionPayload.getConditionType()) {
            case GREATER_THAN -> String.format("%s is strictly greater than %s", tradingPair, targetPrice);
            case LESS_THAN -> String.format("%s is strictly less than %s", tradingPair, targetPrice);
            case CROSSED_UP -> String.format("%s crossed up %s", tradingPair, targetPrice);
            case CROSSED_DOWN -> String.format("%s crossed down %s", tradingPair, targetPrice);
            case PERCENT_UP -> String.format("%s increased by %s%%", tradingPair, resolvePercentage(conditionPayload));
            case PERCENT_DOWN -> String.format("%s dropped by %s%%", tradingPair, resolvePercentage(conditionPayload));
            case TRAILING_DROP -> String.format("%s fell %s%% from its peak", tradingPair, resolvePercentage(conditionPayload));
            case TRAILING_RISE -> String.format("%s rose %s%% from its local low", tradingPair, resolvePercentage(conditionPayload));
        };
    }

    private BigDecimal resolveTargetPrice(ConditionPayload conditionPayload) {
        return switch (conditionPayload) {
            case PercentagePayload percentagePayload -> percentagePayload.getInitialPrice();
            case TargetPricePayload targetPricePayload -> targetPricePayload.getTargetPrice();
            case TrailingPayload trailingPayload -> trailingPayload.getReferencePrice();
            default -> throw new IllegalArgumentException("Unknown payload type: " + conditionPayload.getClass());
        };
    }

    private BigDecimal resolvePercentage(ConditionPayload conditionPayload) {
        return switch (conditionPayload) {
            case PercentagePayload percentagePayload -> percentagePayload.getPercentageChange();
            case TrailingPayload trailingPayload -> trailingPayload.getTrailingPercentage();
            default -> throw new IllegalArgumentException("Unknown payload type: " + conditionPayload.getClass());
        };
    }
}
