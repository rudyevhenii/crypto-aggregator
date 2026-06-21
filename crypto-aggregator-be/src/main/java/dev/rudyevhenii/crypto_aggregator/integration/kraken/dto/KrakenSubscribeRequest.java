package dev.rudyevhenii.crypto_aggregator.integration.kraken.dto;

import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KrakenSubscribeRequest {

    private static final String SUBSCRIBE_MESSAGE = "subscribe";

    private String method;
    private KrakenSubscriptionParams params;

    @Builder
    record KrakenSubscriptionParams(String channel, List<String> symbol) {

        public static KrakenSubscriptionParams create(Map<TradingPair, String> tradingPairMap) {
            return KrakenSubscriptionParams.builder()
                    .channel("ticker")
                    .symbol(getTradingPairs(tradingPairMap))
                    .build();
        }

        private static List<String> getTradingPairs(Map<TradingPair, String> tradingPairProperties) {
            return tradingPairProperties.keySet().stream()
                    .map(pair -> pair.name().replace("_", "/"))
                    .toList();
        }
    }

    public static KrakenSubscribeRequest create(Map<TradingPair, String> tradingPairMap) {
        return KrakenSubscribeRequest.builder()
                .method(SUBSCRIBE_MESSAGE)
                .params(KrakenSubscriptionParams.create(tradingPairMap))
                .build();
    }
}
