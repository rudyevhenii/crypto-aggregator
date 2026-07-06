package dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.BinanceTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.BinanceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HistoricalBinanceMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.BINANCE;

    private final BinanceProperties properties;

    public Ticker24hDto toTickerDto(BinanceTicker24hResponse res) {
        return Ticker24hDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(resolveTradingPair(res.tradingPair()))
                .lastPrice(res.lastPrice())
                .priceChangePercent24h(res.priceChangePercent24h())
                .high24h(res.highPrice24h())
                .low24h(res.lowPrice24h())
                .volume24h(res.volume24h())
                .build();
    }

    public List<HistoricalPriceDto> toHistoricalPriceDto(List<List<Number>> klines) {
        if (klines == null || klines.isEmpty()) {
            return Collections.emptyList();
        }

        return klines.stream()
                .map(kline -> {
                    long timeInSeconds = kline.get(0).longValue();
                    Instant openTime = Instant.ofEpochMilli(timeInSeconds);

                    return HistoricalPriceDto.builder()
                            .openTime(openTime)
                            .open(new BigDecimal(kline.get(1).toString()))
                            .high(new BigDecimal(kline.get(2).toString()))
                            .low(new BigDecimal(kline.get(3).toString()))
                            .close(new BigDecimal(kline.get(4).toString()))
                            .volume(new BigDecimal(kline.get(5).toString()))
                            .build();
                })
                .toList();
    }

    private TradingPair resolveTradingPair(String rawTradingPair) {
        Map<TradingPair, String> tradingPairMap = properties.tradingPair();

        return tradingPairMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(rawTradingPair))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
