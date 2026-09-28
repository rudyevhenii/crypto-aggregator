package dev.rudyevhenii.crypto_aggregator.core.config;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.ConditionPayloadRqDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public JsonMapper jacksonObjectMapperBuilder() {
        return JsonMapper.builder()
                .addMixIn(ConditionPayloadRqDto.class, ConditionPayloadMixIn.class)
                .build();
    }
}
