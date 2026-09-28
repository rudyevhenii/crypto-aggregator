package dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "conditionType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TargetPricePayload.class, name = "GREATER_THAN"),
        @JsonSubTypes.Type(value = TargetPricePayload.class, name = "LESS_THAN"),
        @JsonSubTypes.Type(value = TargetPricePayload.class, name = "CROSSED_UP"),
        @JsonSubTypes.Type(value = TargetPricePayload.class, name = "CROSSED_DOWN"),
        @JsonSubTypes.Type(value = PercentagePayload.class, name = "PERCENT_UP"),
        @JsonSubTypes.Type(value = PercentagePayload.class, name = "PERCENT_DOWN"),
        @JsonSubTypes.Type(value = TrailingPayload.class, name = "TRAILING_DROP"),
        @JsonSubTypes.Type(value = TrailingPayload.class, name = "TRAILING_RISE"),
})
public interface ConditionPayload {

    ConditionType getConditionType();
}
