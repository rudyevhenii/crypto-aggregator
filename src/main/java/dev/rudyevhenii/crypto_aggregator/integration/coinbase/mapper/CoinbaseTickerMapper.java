package dev.rudyevhenii.crypto_aggregator.integration.coinbase.mapper;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto.CoinbaseTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.util.ExchangeUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class CoinbaseTickerMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.COINBASE;

    public Ticker24hDto toTickerDto(CoinbaseTicker24hResponse res, TradingPair pair) {
        BigDecimal lastPrice = res.lastPrice();
        BigDecimal openPrice24h = res.openPrice24h();

        return Ticker24hDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(pair)
                .lastPrice(lastPrice)
                .priceChangePercent24h(ExchangeUtils.calculatePercentChange(lastPrice, openPrice24h))
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
                    Instant openTime = Instant.ofEpochSecond(timeInSeconds);

                    return HistoricalPriceDto.builder()
                            .openTime(openTime)
                            .low(new BigDecimal(kline.get(1).toString()))
                            .high(new BigDecimal(kline.get(2).toString()))
                            .open(new BigDecimal(kline.get(3).toString()))
                            .close(new BigDecimal(kline.get(4).toString()))
                            .volume(new BigDecimal(kline.get(5).toString()))
                            .build();
                })
                .sorted(Comparator.comparing(HistoricalPriceDto::openTime))
                .toList();
    }

    public LivePriceDto toLivePriceDto(CoinbaseTickerWsResponse res, TradingPair tradingPair) {
        BigDecimal lastPrice = res.lastPrice();
        BigDecimal openPrice24h = res.openPrice24h();

        return LivePriceDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(tradingPair)
                .lastPrice(lastPrice)
                .priceChangePercent24h(ExchangeUtils.calculatePercentChange(lastPrice, openPrice24h))
                .highPrice24h(res.highPrice24h())
                .lowPrice24h(res.lowPrice24h())
                .volume24h(res.volume24h())
                .timestamp(res.timestamp())
                .build();
    }
}
