package dev.rudyevhenii.crypto_aggregator.exchange.live.mapper;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.util.ExchangeUtils;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LiveCoinbaseMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.COINBASE;

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
