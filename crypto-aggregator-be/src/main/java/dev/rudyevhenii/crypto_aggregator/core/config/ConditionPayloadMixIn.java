package dev.rudyevhenii.crypto_aggregator.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "conditionType",
        visible = true
)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ConditionPayloadMixIn {
}
