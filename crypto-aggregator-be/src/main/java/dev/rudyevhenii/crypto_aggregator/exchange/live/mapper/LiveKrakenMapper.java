package dev.rudyevhenii.crypto_aggregator.exchange.live.mapper;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.KrakenTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKrakenMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;

    public LivePriceDto toLivePriceDto(KrakenTickerWsResponse res, TradingPair tradingPair) {
        KrakenTickerWsResponse.KrakenTickerData tickerData = res.data().getFirst();

        return LivePriceDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(tradingPair)
                .lastPrice(tickerData.lastPrice())
                .priceChangePercent24h(tickerData.priceChangePercent24h())
                .highPrice24h(tickerData.high24h())
                .lowPrice24h(tickerData.low24h())
                .volume24h(tickerData.volume24h())
                .timestamp(tickerData.timestamp())
                .build();
    }
}
