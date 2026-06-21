package dev.rudyevhenii.crypto_aggregator.integration.binance.dto;

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
public class BinanceSubscribeRequest {

    private static final String SUBSCRIBE_MESSAGE = "SUBSCRIBE";

    private String method;
    private List<String> params;
    private Integer id;

    public static BinanceSubscribeRequest create(Map<TradingPair, String> tradingPairMap) {
        return BinanceSubscribeRequest.builder()
                .method(SUBSCRIBE_MESSAGE)
                .params(getTradingPairs(tradingPairMap))
                .id(1)
                .build();
    }

    private static List<String> getTradingPairs(Map<TradingPair, String> tradingPairMap) {
        return tradingPairMap.values().stream()
                .map(String::toLowerCase)
                .map("%s@ticker"::formatted)
                .toList();
    }
}
