package dev.rudyevhenii.crypto_aggregator.exchange.live.mapper;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.BinanceTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LiveBinanceMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.BINANCE;

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
}
