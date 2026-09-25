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
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.PercentagePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TrailingPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.OneTimeTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.RecurringTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.price_alert.repository.DefaultPriceAlertRepositoryTest.TestResources.*;
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
class DefaultPriceAlertRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PriceAlertRepository repository;

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml")
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/then/created_price_alert.yaml")
    void givenPriceAlert_create_shouldCreateNewPriceAlert() {
        PriceAlert result = repository.create(buildFirstPriceAlert());
        assertThat(result).isEqualTo(buildFirstPriceAlert());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/then/updated_price_alert.yaml")
    void givenExistingPriceAlert_update_shouldUpdatePriceAlert() {
        PriceAlert result = repository.update(buildUpdatedFirstPriceAlert());
        assertThat(result).isEqualTo(buildUpdatedFirstPriceAlert());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    void givenUserIdAndId_findById_shouldReturnPriceAlert() {
        Optional<PriceAlert> result = repository.findById(USER_ID, TestResources.ID_1);
        assertThat(result).contains(buildFirstPriceAlert());
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentIds")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    void givenNonExistentIds_findById_shouldReturnEmptyOptional(UUID userId, UUID id) {
        Optional<PriceAlert> result = repository.findById(userId, id);
        assertThat(result).isEmpty();
    }

    static Stream<Arguments> provideNonExistentIds() {
        return Stream.of(
                Arguments.of(NON_EXISTENT_USER_ID, NON_EXISTENT_ID),
                Arguments.of(USER_ID, NON_EXISTENT_ID),
                Arguments.of(NON_EXISTENT_USER_ID, TestResources.ID_1)
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    void givenNothing_findAll_shouldFindAllPriceAlerts() {
        List<PriceAlert> result = repository.findAll(USER_ID);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(buildPriceAlertList());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    void givenNothing_findAllActive_shouldFindActivePriceAlerts() {
        List<PriceAlert> result = repository.findAllActive();
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(buildFirstPriceAlert()));
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/then/expired_price_alert.yaml")
    void givenCurrentTime_deactivateExpiredAlerts_shouldReturnExpiredAlerts() {
        List<PriceAlert> result = repository.deactivateExpiredAlerts(NOW);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(buildExpiredFirstPriceAlert()));
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/repository/datasets/then/deleted_price_alert.yaml")
    void givenId_deleteById_shouldDeletePriceAlert() {
        repository.deleteById(ID_1);
    }

    static class TestResources {

        static final UUID ID_1 = UUID.fromString("50000000-0000-0000-0000-000000000005");
        static final UUID ID_2 = UUID.fromString("51111111-1111-1111-1111-111111111115");
        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");
        static final Instant EXPIRES_AT = Instant.parse("2026-09-08T12:00:00Z");
        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final BigDecimal TARGET_PRICE = new BigDecimal("85000.00");
        static final BigDecimal INITIAL_PRICE = new BigDecimal("73500.00");
        static final BigDecimal PERCENTAGE_CHANGE = new BigDecimal("3.25");
        static final BigDecimal TRAILING_PERCENTAGE = new BigDecimal("1.58");
        static final BigDecimal REFERENCE_PRICE = new BigDecimal("76480.00");

        static final int COOLDOWN_MINUTES_1 = 5;
        static final int COOLDOWN_MINUTES_2 = 8;

        static final UUID NON_EXISTENT_ID = UUID.fromString("9aaaaaaa-9999-9999-9999-aaaaaaaaaaa9");
        static final UUID NON_EXISTENT_USER_ID = UUID.fromString("9bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb9");

        static PriceAlert buildFirstPriceAlert() {
            return PriceAlert.builder()
                    .id(ID_1)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildFirstTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(buildFirstConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static PriceAlert buildUpdatedFirstPriceAlert() {
            return PriceAlert.builder()
                    .id(ID_1)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildUpdatedFirstTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(buildUpdatedFirstConditionPayload())
                    .expiresAt(null)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static PriceAlert buildExpiredFirstPriceAlert() {
            return PriceAlert.builder()
                    .id(ID_1)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildFirstTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(false)
                    .conditionPayload(buildFirstConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static PriceAlert buildSecondPriceAlert() {
            return PriceAlert.builder()
                    .id(ID_2)
                    .userId(USER_ID)
                    .exchange(Exchange.COINBASE)
                    .tradingPair(TradingPair.ETC_USD)
                    .triggerPolicy(buildSecondTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(false)
                    .conditionPayload(buildSecondConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static List<PriceAlert> buildPriceAlertList() {
            return List.of(
                    buildFirstPriceAlert(),
                    buildSecondPriceAlert()
            );
        }

        private static TriggerPolicy buildFirstTriggerPolicy() {
            return RecurringTriggerPolicy.builder()
                    .cooldownMinutes(COOLDOWN_MINUTES_1)
                    .build();
        }

        private static TriggerPolicy buildUpdatedFirstTriggerPolicy() {
            return new OneTimeTriggerPolicy();
        }

        private static TriggerPolicy buildSecondTriggerPolicy() {
            return RecurringTriggerPolicy.builder()
                    .cooldownMinutes(COOLDOWN_MINUTES_2)
                    .build();
        }

        private static Set<DeliveryMethod> buildDeliveryMethods() {
            return Set.of(DeliveryMethod.EMAIL);
        }

        private static ConditionPayload buildFirstConditionPayload() {
            return TargetPricePayload.builder()
                    .conditionType(ConditionType.GREATER_THAN)
                    .targetPrice(TARGET_PRICE)
                    .build();
        }

        private static ConditionPayload buildUpdatedFirstConditionPayload() {
            return PercentagePayload.builder()
                    .conditionType(ConditionType.PERCENT_UP)
                    .initialPrice(INITIAL_PRICE)
                    .percentageChange(PERCENTAGE_CHANGE)
                    .build();
        }

        private static ConditionPayload buildSecondConditionPayload() {
            return TrailingPayload.builder()
                    .conditionType(ConditionType.TRAILING_DROP)
                    .trailingPercentage(TRAILING_PERCENTAGE)
                    .referencePrice(REFERENCE_PRICE)
                    .build();
        }
    }
}