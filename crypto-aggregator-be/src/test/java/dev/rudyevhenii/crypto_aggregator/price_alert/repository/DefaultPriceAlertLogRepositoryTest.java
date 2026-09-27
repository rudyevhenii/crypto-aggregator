package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.CustomPostgresDataTypeFactory;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.price_alert.repository.DefaultPriceAlertLogRepositoryTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DBRider
@DBUnit(
        dataTypeFactoryClass = CustomPostgresDataTypeFactory.class,
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class DefaultPriceAlertLogRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PriceAlertLogRepository repository;

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert_for_logs.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/then/created_price_alert_log.yaml")
    void givenPriceAlertLog_create_shouldCreatePriceAlertLog() {
        repository.create(buildPriceAlertLog());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert_for_logs.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert_log.yaml"
    })
    void givenPriceAlertLogScrollRequest_findAllAlertLogs_shouldFindAllLogsFromScrollPosition() {
        List<PriceAlertLog> result = repository.findAllAlertLogs(USER_ID, buildScrollRequest());
        assertThat(result)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(List.of(buildPriceAlertLog3(), buildPriceAlertLog2()));
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert_for_logs.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert_log.yaml"
    })
    void givenPriceAlertLogScrollRequest_findAllAlertLogs_shouldFindLogsFromStart() {
        List<PriceAlertLog> result = repository.findAllAlertLogs(USER_ID, buildScrollRequestWithNullCursorFields());
        assertThat(result)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(List.of(buildPriceAlertLog(), buildPriceAlertLog3()));
    }

    static class TestResources {
        static final UUID ID = UUID.fromString("60000000-0000-0000-0000-000000000006");
        static final UUID ID_2 = UUID.fromString("61111111-1111-1111-1111-111111111116");
        static final UUID ID_3 = UUID.fromString("62222222-2222-2222-2222-222222222226");

        static final UUID PRICE_ALERT_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
        static final UUID PRICE_ALERT_ID_2 = UUID.fromString("51111111-1111-1111-1111-111111111115");

        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant CREATED_AT = Instant.parse("2026-08-18T12:00:00Z");
        static final Instant CREATED_AT_2 = Instant.parse("2026-08-16T12:00:00Z");

        static final TradingPair TRADING_PAIR = TradingPair.BTC_USD;
        static final TradingPair TRADING_PAIR_2 = TradingPair.ETH_USD;

        static final BigDecimal TARGET_PRICE = new BigDecimal("78500.00");
        static final BigDecimal TARGET_PRICE_2 = new BigDecimal("2600.00");

        static final BigDecimal TRIGGERED_PRICE = new BigDecimal("78501.40");
        static final BigDecimal TRIGGERED_PRICE_2 = new BigDecimal("2598.70");
        static final BigDecimal TRIGGERED_PRICE_3 = new BigDecimal("78504.90");

        static final String MESSAGE = "%s is strictly greater than %s"
                .formatted(TRADING_PAIR, TARGET_PRICE);
        static final String MESSAGE_2 = "%s is strictly less than %s"
                .formatted(TRADING_PAIR_2, TARGET_PRICE_2);
        static final String MESSAGE_3 = "%s is strictly greater than %s"
                .formatted(TRADING_PAIR, TARGET_PRICE);

        static final Instant LAST_CREATED_AT = Instant.parse("2026-08-17T12:00:00Z");
        static final int LIMIT = 50;
        static final UUID LAST_ID = UUID.fromString("60000000-0000-0000-0000-000000000006");

        static PriceAlertLog buildPriceAlertLog() {
            return PriceAlertLog.builder()
                    .id(ID)
                    .priceAlertId(PRICE_ALERT_ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TRADING_PAIR)
                    .conditionType(ConditionType.GREATER_THAN)
                    .triggeredPrice(TRIGGERED_PRICE)
                    .message(MESSAGE)
                    .deliveryMethods(buildDeliveryMethods())
                    .createdAt(CREATED_AT)
                    .build();
        }

        static PriceAlertLog buildPriceAlertLog2() {
            return PriceAlertLog.builder()
                    .id(ID_2)
                    .priceAlertId(PRICE_ALERT_ID_2)
                    .userId(USER_ID)
                    .exchange(Exchange.COINBASE)
                    .tradingPair(TRADING_PAIR_2)
                    .conditionType(ConditionType.LESS_THAN)
                    .triggeredPrice(TRIGGERED_PRICE_2)
                    .message(MESSAGE_2)
                    .deliveryMethods(buildDeliveryMethods())
                    .createdAt(CREATED_AT_2)
                    .build();
        }

        static PriceAlertLog buildPriceAlertLog3() {
            return PriceAlertLog.builder()
                    .id(ID_3)
                    .priceAlertId(PRICE_ALERT_ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TRADING_PAIR)
                    .conditionType(ConditionType.GREATER_THAN)
                    .triggeredPrice(TRIGGERED_PRICE_3)
                    .message(MESSAGE_3)
                    .deliveryMethods(buildDeliveryMethods())
                    .createdAt(CREATED_AT_2)
                    .build();
        }

        static PriceAlertLogScrollRequest buildScrollRequest() {
            return PriceAlertLogScrollRequest.builder()
                    .lastCreatedAt(LAST_CREATED_AT)
                    .lastId(LAST_ID)
                    .limit(LIMIT)
                    .build();
        }

        static PriceAlertLogScrollRequest buildScrollRequestWithNullCursorFields() {
            return PriceAlertLogScrollRequest.builder()
                    .lastCreatedAt(null)
                    .lastId(null)
                    .limit(2)
                    .build();
        }

        private static Set<DeliveryMethod> buildDeliveryMethods() {
            return Set.of(DeliveryMethod.EMAIL);
        }
    }
}