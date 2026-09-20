package dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.rudyevhenii.crypto_aggregator.price_alert.TriggerType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneTimeTriggerPolicy implements TriggerPolicy {

    @Override
    public TriggerType getTriggerType() {
        return TriggerType.ONE_TIME;
    }
}
