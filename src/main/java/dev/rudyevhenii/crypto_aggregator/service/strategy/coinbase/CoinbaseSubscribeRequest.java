package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

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
public class CoinbaseSubscribeRequest {
    private String method;
    private List<String> params;
    private Integer id;

    public static CoinbaseSubscribeRequest create(CryptoProperties.ExchangeProperties cryptoProperties) {
        return CoinbaseSubscribeRequest.builder()
                .method("SUBSCRIBE")
                .params(getTradingPairs(cryptoProperties.tradingPair()))
                .id(1)
                .build();
    }

    private static List<String> getTradingPairs(Map<TradingPair, String> cryptoProperties) {
        return cryptoProperties.values().stream().toList();
    }
}
