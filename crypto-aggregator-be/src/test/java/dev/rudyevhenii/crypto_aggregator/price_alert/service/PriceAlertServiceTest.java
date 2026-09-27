package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertInMemoryCacheManager;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.PercentagePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.OneTimeTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.RecurringTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertDomainMapper;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepository repository;

    @Spy
    private PriceAlertDomainMapper mapper;

    @Mock
    private PriceAlertInMemoryCacheManager inMemoryCacheManager;

    @Mock
    private UserContext userContext;

    @Mock
    private GeneratorUtils generator;

    @InjectMocks
    private PriceAlertServiceImpl service;

    @Test
    void givenPriceAlertRequest_create_shouldCreatePriceAlert() {
        when(generator.uuid()).thenReturn(ID);
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(CREATED_AT, CREATED_AT);
        when(repository.create(any(PriceAlert.class))).thenReturn(buildPriceAlert());

        PriceAlert result = service.create(buildPriceAlertRequest());

        verify(inMemoryCacheManager).addAlertToCache(buildPriceAlert());
        assertThat(result).isEqualTo(buildPriceAlert());
    }

    @Test
    void givenPriceAlertUpdateRequest_update_shouldUpdateExistingPriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildPriceAlert()));
        doNothing().when(mapper).toUpdateDomain(buildPriceAlertUpdateRequest(), buildPriceAlert(), generator);
        when(repository.update(any(PriceAlert.class))).thenReturn(buildUpdatedPriceAlert());

        PriceAlert result = service.update(ID, buildPriceAlertUpdateRequest());

        verify(inMemoryCacheManager).updateAlertInCache(buildUpdatedPriceAlert());
        assertThat(result).isEqualTo(buildUpdatedPriceAlert());
    }

    @Test
    void givenId_getPriceAlertById_shouldReturnPriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildPriceAlert()));

        PriceAlert result = service.getPriceAlertById(ID);

        assertThat(result).isEqualTo(buildPriceAlert());
    }

    @Test
    void givenNonExistentId_getPriceAlertById_shouldThrowException() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPriceAlertById(ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenNothing_getAllPriceAlerts_shouldReturnPriceAlerts() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findAll(USER_ID)).thenReturn(List.of(buildPriceAlert()));

        List<PriceAlert> result = service.getAllPriceAlerts();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(buildPriceAlert()));
    }

    @Test
    void givenNothing_getAllActive_shouldReturnAllActivePriceAlerts() {
        when(repository.findAllActive()).thenReturn(List.of(buildPriceAlert()));

        List<PriceAlert> result = service.getAllActive();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(List.of(buildPriceAlert()));
    }

    @Test
    void givenId_activate_shouldActivatePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildDeactivatedPriceAlert()));
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.update(any(PriceAlert.class))).thenReturn(buildActivatedPriceAlert());

        service.activate(ID);

        verify(inMemoryCacheManager).upsertAlertInCache(buildActivatedPriceAlert());
    }

    @Test
    void givenId_deactivate_shouldDeactivatePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildActivatedPriceAlert()));
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.update(any(PriceAlert.class))).thenReturn(buildDeactivatedPriceAlert());

        service.deactivate(ID);

        verify(inMemoryCacheManager).removeAlertFromCache(buildDeactivatedPriceAlert());
    }

    @Test
    void givenId_deactivateForUser_shouldDeactivatePriceAlert() {
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildActivatedPriceAlert()));
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.update(any(PriceAlert.class))).thenReturn(buildDeactivatedPriceAlert());

        service.deactivateForUser(USER_ID, ID);

        verify(inMemoryCacheManager).removeAlertFromCache(buildDeactivatedPriceAlert());
    }

    @Test
    void givenId_deleteById_shouldDeletePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findById(USER_ID, ID)).thenReturn(Optional.of(buildPriceAlert()));

        service.deleteById(ID);

        verify(inMemoryCacheManager).removeAlertFromCache(buildPriceAlert());
        verify(repository).deleteById(ID);
    }

    static class TestResources {
        static final UUID ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant EXPIRES_AT = Instant.parse("2026-09-08T12:00:00Z");
        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final BigDecimal TARGET_PRICE = new BigDecimal("85000.00");
        static final BigDecimal INITIAL_PRICE = new BigDecimal("73500.00");
        static final BigDecimal PERCENTAGE_CHANGE = new BigDecimal("3.25");
        static final int COOLDOWN_MINUTES = 5;

        static PriceAlertRequest buildPriceAlertRequest() {
            return PriceAlertRequest.builder()
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .conditionPayload(buildConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .build();
        }

        static PriceAlertUpdateRequest buildPriceAlertUpdateRequest() {
            return PriceAlertUpdateRequest.builder()
                    .triggerPolicy(buildUpdatedTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .conditionPayload(buildUpdatedConditionPayload())
                    .expiresAt(null)
                    .build();
        }

        static PriceAlert buildPriceAlert() {
            return PriceAlert.builder()
                    .id(ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(buildConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static PriceAlert buildDeactivatedPriceAlert() {
            return PriceAlert.builder()
                    .id(ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(false)
                    .conditionPayload(buildConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static PriceAlert buildActivatedPriceAlert() {
            return PriceAlert.builder()
                    .id(ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(buildConditionPayload())
                    .expiresAt(EXPIRES_AT)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static PriceAlert buildUpdatedPriceAlert() {
            return PriceAlert.builder()
                    .id(ID)
                    .userId(USER_ID)
                    .exchange(Exchange.BINANCE)
                    .tradingPair(TradingPair.BTC_USD)
                    .triggerPolicy(buildUpdatedTriggerPolicy())
                    .deliveryMethods(buildDeliveryMethods())
                    .active(true)
                    .conditionPayload(buildUpdatedConditionPayload())
                    .expiresAt(null)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        private static TriggerPolicy buildTriggerPolicy() {
            return RecurringTriggerPolicy.builder()
                    .cooldownMinutes(COOLDOWN_MINUTES)
                    .build();
        }

        private static TriggerPolicy buildUpdatedTriggerPolicy() {
            return new OneTimeTriggerPolicy();
        }

        private static Set<DeliveryMethod> buildDeliveryMethods() {
            return Set.of(DeliveryMethod.EMAIL);
        }

        private static ConditionPayload buildConditionPayload() {
            return TargetPricePayload.builder()
                    .conditionType(ConditionType.GREATER_THAN)
                    .targetPrice(TARGET_PRICE)
                    .build();
        }

        private static ConditionPayload buildUpdatedConditionPayload() {
            return PercentagePayload.builder()
                    .conditionType(ConditionType.PERCENT_UP)
                    .initialPrice(INITIAL_PRICE)
                    .percentageChange(PERCENTAGE_CHANGE)
                    .build();
        }
    }
}