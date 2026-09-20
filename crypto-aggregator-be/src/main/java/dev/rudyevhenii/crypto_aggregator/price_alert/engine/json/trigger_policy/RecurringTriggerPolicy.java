package dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.rudyevhenii.crypto_aggregator.price_alert.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringTriggerPolicy implements TriggerPolicy {
    private int cooldownMinutes;

    @Override
    public TriggerType getTriggerType() {
        return TriggerType.RECURRING;
    }
}
