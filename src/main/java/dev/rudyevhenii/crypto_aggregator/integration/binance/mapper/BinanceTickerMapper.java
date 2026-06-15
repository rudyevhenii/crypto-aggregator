package dev.rudyevhenii.crypto_aggregator.integration.binance.mapper;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.binance.dto.BinanceTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.binance.dto.BinanceTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.integration.binance.properties.BinanceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BinanceTickerMapper {

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
                .timestamp(Instant.now())
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

    public LivePriceDto toLivePriceDto(BinanceTickerWsResponse res, TradingPair tradingPair) {
        return LivePriceDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(tradingPair)
                .lastPrice(res.lastPrice())
                .priceChangePercent24h(res.priceChangePercent24h())
                .highPrice24h(res.high24h())
                .lowPrice24h(res.low24h())
                .volume24h(res.volume24h())
                .timestamp(Instant.ofEpochMilli(res.eventTime()))
                .build();
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
