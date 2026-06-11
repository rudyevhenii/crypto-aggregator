package dev.rudyevhenii.crypto_aggregator.service.strategy.binance;

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
public class BinanceSubscribeRequest {
    private String method;
    private List<String> params;
    private Integer id;

    public static BinanceSubscribeRequest create(CryptoProperties.ExchangeProperties cryptoProperties) {
        return BinanceSubscribeRequest.builder()
                .method("SUBSCRIBE")
                .params(getTradingPairs(cryptoProperties.tradingPair()))
                .id(1)
                .build();
    }

    private static List<String> getTradingPairs(Map<TradingPair, String> cryptoProperties) {
        return cryptoProperties.values().stream()
                .map(String::toLowerCase)
                .map("%s@ticker"::formatted)
                .toList();
    }
}
