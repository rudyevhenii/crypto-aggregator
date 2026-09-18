package dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetPricePayload implements ConditionPayload {
    private ConditionType conditionType;
    private BigDecimal targetPrice;
}
