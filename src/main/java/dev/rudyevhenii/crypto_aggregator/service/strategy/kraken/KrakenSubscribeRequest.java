package dev.rudyevhenii.crypto_aggregator.service.strategy.kraken;

import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KrakenSubscribeRequest {
    private String method;
    private KrakenSubscriptionParams params;

    @Builder
    record KrakenSubscriptionParams(String channel, List<String> symbol) {

        public static KrakenSubscriptionParams create(CryptoProperties.ExchangeProperties cryptoProperties) {
            return KrakenSubscriptionParams.builder()
                    .channel("ticker")
                    .symbol(getTradingPairs(cryptoProperties.tradingPair()))
                    .build();
        }

        private static List<String> getTradingPairs(Map<TradingPair, String> tradingPairProperties) {
            return tradingPairProperties.values().stream().toList();
        }
    }

    public static KrakenSubscribeRequest create(CryptoProperties.ExchangeProperties cryptoProperties) {
        return KrakenSubscribeRequest.builder()
                .method("subscribe")
                .params(KrakenSubscriptionParams.create(cryptoProperties))
                .build();
    }
}
