package dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CoinbaseSubscribeRequest {

    private static final String SUBSCRIBE_MESSAGE = "subscribe";
    private static final String CHANNEL = "ticker";

    private String type;

    @JsonProperty("product_ids")
    private List<String> productIds;
    private List<String> channels;

    public static CoinbaseSubscribeRequest create(Map<TradingPair, String> tradingPairMap) {
        return CoinbaseSubscribeRequest.builder()
                .type(SUBSCRIBE_MESSAGE)
                .productIds(getTradingPairs(tradingPairMap))
                .channels(List.of(CHANNEL))
                .build();
    }

    private static List<String> getTradingPairs(Map<TradingPair, String> tradingPairMap) {
        return tradingPairMap.values().stream().toList();
    }
}
