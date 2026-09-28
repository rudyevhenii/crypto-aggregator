package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.PercentagePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TrailingPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.RecurringTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertLogServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceAlertLogServiceTest {

    @Mock
    private PriceAlertLogRepository repository;

    @Mock
    private UserContext userContext;

    @Mock
    private GeneratorUtils generator;

    @InjectMocks
    private PriceAlertLogServiceImpl service;

    @ParameterizedTest
    @MethodSource("conditionTypes")
    void givenPriceAlert_create_shouldCreatePriceAlertLog(ConditionPayload conditionPayload, String message) {
        when(generator.uuid()).thenReturn(ID);
        when(generator.now()).thenReturn(CREATED_AT);

        service.create(buildPriceAlert(conditionPayload), TRIGGERED_PRICE);

        verify(repository).create(buildPriceAlertLog(conditionPayload.getConditionType(), message));
    }

    public static Stream<Arguments> conditionTypes() {
        return Stream.of(
                Arguments.of(
                        buildTargetPayload(ConditionType.GREATER_THAN),
                        "BTC_USD is strictly greater than 78500.00"
                ),
                Arguments.of(
                        buildTargetPayload(ConditionType.LESS_THAN),
                        "BTC_USD is strictly less than 78500.00"
                ),
                Arguments.of(
                        buildTargetPayload(ConditionType.CROSSED_UP),
                        "BTC_USD crossed up 78500.00"
                ),
                Arguments.of(
                        buildTargetPayload(ConditionType.CROSSED_DOWN),
                        "BTC_USD crossed down 78500.00"
                ),
                Arguments.of(
                        buildPercentagePayload(ConditionType.PERCENT_UP),
                        "BTC_USD increased by 3.5%"
                ),
                Arguments.of(
                        buildPercentagePayload(ConditionType.PERCENT_DOWN),
                        "BTC_USD dropped by 3.5%"
                ),
                Arguments.of(
                        buildTrailingPayload(ConditionType.TRAILING_DROP),
                        "BTC_USD fell 2.8% from its peak"
                ),
                Arguments.of(
                        buildTrailingPayload(ConditionType.TRAILING_RISE),
                        "BTC_USD rose 2.8% from its local low"
                )
        );
    }

    @Test
    void givenScrollRequest_getAllAlertLogs_shouldCreatePriceAlertLog() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findAllAlertLogs(USER_ID, buildScrollRequest())).thenReturn(List.of(buildPriceAlertLog()));

        List<PriceAlertLog> result = service.getAllAlertLogs(buildScrollRequest());

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(buildPriceAlertLog()));
    }

    static class TestResources {

        static final UUID ID = UUID.fromString("60000000-0000-0000-0000-000000000006");
        static final UUID PRICE_ALERT_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");

        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant EXPIRES_AT = Instant.parse("2026-09-08T12:00:00Z");
        static final Instant CREATED_AT = Instant.parse("2026-08-18T12:00:00Z");

        static final Exchange EXCHANGE = Exchange.BINANCE;
        static final TradingPair TRADING_PAIR = TradingPair.BTC_USD;

        static final BigDecimal TARGET_PRICE = new BigDecimal("78500.00");
        static final BigDecimal INITIAL_PRICE = new BigDecimal("81300.00");
        static final BigDecimal REFERENCE_PRICE = new BigDecimal("79800.00");

        static final BigDecimal PERCENTAGE_CHANGE = new BigDecimal("3.5");
        static final BigDecimal TRAILING_PERCENTAGE = new BigDecimal("2.8");

        static final BigDecimal TRIGGERED_PRICE = new BigDecimal("78501.40");

        static final String MESSAGE = "%s is strictly greater than %s".formatted(TRADING_PAIR, TARGET_PRICE);

        static final int COOLDOWN_MINUTES_1 = 5;

        static final Instant LAST_CREATED_AT = Instant.parse("2026-08-17T12:00:00Z");
        static final int LIMIT = 50;
        static final UUID LAST_ID = UUID.fromString("60000000-0000-0000-0000-000000000006");

        static PriceAlertLog buildPriceAlertLog() {
            return PriceAlertLog.builder()
                    .id(ID)
                    .priceAlertId(PRICE_ALERT_ID)
                    .userId(USER_ID)
                    .exchange(EXCHANGE)
                    .tradingPair(TRADING_PAIR)
                    .conditionType(ConditionType.GREATER_THAN)
                    .triggeredPrice(TRIGGERED_PRICE)
                    .message(MESSAGE)
                    .deliveryMethods(buildDeliveryMethods())
                    .createdAt(CREATED_AT)
                    .build();
        }

        static PriceAlertLog buildPriceAlertLog(ConditionType conditionType, String message) {
            return PriceAlertLog.builder()
                    .id(ID)
                    .priceAlertId(PRICE_ALERT_ID)
                    .userId(USER_ID)
                    .exchange(EXCHANGE)
                    .tradingPair(TRADING_PAIR)
                    .conditionType(conditionType)
                    .triggeredPrice(TRIGGERED_PRICE)
                    .message(message)
                    .deliveryMethods(buildDeliveryMethods())
                    .createdAt(CREATED_AT)
                    .build();
        }

        static PriceAlert buildPriceAlert(ConditionPayload conditionPayload) {
            return PriceAlert.builder()
                    .id(PRICE_ALERT_ID)
                    .userId(USER_ID)
                    .exchange(EXCHANGE)
                    .tradingPair(TRADING_PAIR)
                    .triggerPolicy(buildTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(conditionPayload)
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static ConditionPayload buildTargetPayload(ConditionType conditionType) {
            return TargetPricePayload.builder()
                    .conditionType(conditionType)
                    .targetPrice(TARGET_PRICE)
                    .build();
        }

        static ConditionPayload buildPercentagePayload(ConditionType conditionType) {
            return PercentagePayload.builder()
                    .conditionType(conditionType)
                    .initialPrice(INITIAL_PRICE)
                    .percentageChange(PERCENTAGE_CHANGE)
                    .build();
        }

        static ConditionPayload buildTrailingPayload(ConditionType conditionType) {
            return TrailingPayload.builder()
                    .conditionType(conditionType)
                    .trailingPercentage(TRAILING_PERCENTAGE)
                    .referencePrice(REFERENCE_PRICE)
                    .build();
        }

        static PriceAlertLogScrollRequest buildScrollRequest() {
            return PriceAlertLogScrollRequest.builder()
                    .lastCreatedAt(LAST_CREATED_AT)
                    .lastId(LAST_ID)
                    .limit(LIMIT)
                    .build();
        }

        private static TriggerPolicy buildTriggerPolicy() {
            return RecurringTriggerPolicy.builder()
                    .cooldownMinutes(COOLDOWN_MINUTES_1)
                    .build();
        }

        private static Set<DeliveryMethod> buildDeliveryMethods() {
            return Set.of(DeliveryMethod.EMAIL);
        }
    }
}