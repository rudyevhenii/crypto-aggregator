package dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto;

import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CoinbaseSubscribeRequest {

    private static final String SUBSCRIBE_MESSAGE = "SUBSCRIBE";

    private String method;
    private List<String> params;
    private Integer id;

    public static CoinbaseSubscribeRequest create(Map<TradingPair, String> tradingPairMap) {
        return CoinbaseSubscribeRequest.builder()
                .method(SUBSCRIBE_MESSAGE)
                .params(getTradingPairs(tradingPairMap))
                .id(1)
                .build();
    }

    private static List<String> getTradingPairs(Map<TradingPair, String> tradingPairMap) {
        return tradingPairMap.values().stream().toList();
    }
}
