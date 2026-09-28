package dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.rudyevhenii.crypto_aggregator.price_alert.TriggerType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "triggerType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OneTimeTriggerPolicy.class, name = "ONE_TIME"),
        @JsonSubTypes.Type(value = RecurringTriggerPolicy.class, name = "RECURRING")
})
public interface TriggerPolicy {

    TriggerType getTriggerType();
}
