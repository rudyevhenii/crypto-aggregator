package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.ConditionPayloadRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.OneTimeTriggerPolicyRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PercentagePayloadRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertUpdateRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.RecurringTriggerPolicyRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.TargetPricePayloadRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.TrailingPayloadRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.TriggerPolicyRqDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.PercentagePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TrailingPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.OneTimeTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.RecurringTriggerPolicy;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertMapper {

    PriceAlertRequest map(PriceAlertRequestRqDto requestRqDto);

    PriceAlertUpdateRequest map(PriceAlertUpdateRequestRqDto requestRqDto);

    PriceAlertRqDto map(PriceAlert priceAlert);

    default TriggerPolicy mapTriggerPolicy(TriggerPolicyRqDto triggerPolicy) {
        return switch (triggerPolicy) {
            case OneTimeTriggerPolicyRqDto oneTime -> map(oneTime);
            case RecurringTriggerPolicyRqDto recurring -> map(recurring);
            default -> throw new IllegalArgumentException("Unknown trigger type: " + triggerPolicy.getClass());
        };
    }

    OneTimeTriggerPolicy map(OneTimeTriggerPolicyRqDto oneTime);

    RecurringTriggerPolicy map(RecurringTriggerPolicyRqDto recurring);

    default TriggerPolicyRqDto mapTriggerPolicy(TriggerPolicy triggerPolicy) {
        return switch (triggerPolicy) {
            case OneTimeTriggerPolicy oneTime -> map(oneTime);
            case RecurringTriggerPolicy recurring -> map(recurring);
            default -> throw new IllegalArgumentException("Unknown trigger type: " + triggerPolicy.getClass());
        };
    }

    OneTimeTriggerPolicyRqDto map(OneTimeTriggerPolicy oneTime);

    RecurringTriggerPolicyRqDto map(RecurringTriggerPolicy recurring);

    default ConditionPayload mapConditionPayload(ConditionPayloadRqDto conditionPayload) {
        return switch (conditionPayload) {
            case TargetPricePayloadRqDto target -> map(target);
            case PercentagePayloadRqDto percentage -> map(percentage);
            case TrailingPayloadRqDto trailing -> map(trailing);
            default -> throw new IllegalArgumentException("Unknown payload type: " + conditionPayload.getClass());
        };
    }

    TargetPricePayload map(TargetPricePayloadRqDto target);

    PercentagePayload map(PercentagePayloadRqDto percentage);

    TrailingPayload map(TrailingPayloadRqDto trailing);

    default ConditionPayloadRqDto mapConditionPayload(ConditionPayload conditionPayload) {
        return switch (conditionPayload) {
            case TargetPricePayload target -> map(target);
            case PercentagePayload percentage -> map(percentage);
            case TrailingPayload trailing -> map(trailing);
            default -> throw new IllegalArgumentException("Unknown payload type: " + conditionPayload.getClass());
        };
    }

    TargetPricePayloadRqDto map(TargetPricePayload target);

    PercentagePayloadRqDto map(PercentagePayload percentage);

    TrailingPayloadRqDto map(TrailingPayload trailing);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }

    default Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }
}
