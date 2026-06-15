package dev.rudyevhenii.crypto_aggregator.integration.kraken.mapper;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenOhlcResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.properties.KrakenProperties;
import dev.rudyevhenii.crypto_aggregator.util.ExchangeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KrakenTickerMapper {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;

    private final KrakenProperties properties;

    public Ticker24hDto toTickerDto(KrakenTicker24hResponse res) {
        Map.Entry<String, KrakenTicker24hResponse.KrakenTickerData> dataEntry = res.result().entrySet()
                .iterator().next();
        String rawTradingPair = dataEntry.getKey();
        KrakenTicker24hResponse.KrakenTickerData tickerData = dataEntry.getValue();

        BigDecimal lastPrice = new BigDecimal(tickerData.lastPrice().getFirst());
        BigDecimal openPrice24h = tickerData.openPrice24h();

        return Ticker24hDto.builder()
                .exchange(EXCHANGE_TYPE)
                .tradingPair(resolveTradingPair(rawTradingPair))
                .lastPrice(lastPrice)
                .priceChangePercent24h(ExchangeUtils.calculatePercentChange(lastPrice, openPrice24h))
                .high24h(new BigDecimal(tickerData.highPrice24h().get(1)))
                .low24h(new BigDecimal(tickerData.lowPrice24h().get(1)))
                .volume24h(new BigDecimal(tickerData.volume24h().get(1)))
                .timestamp(Instant.now())
                .build();
    }

    public List<HistoricalPriceDto> toHistoricalPriceDto(KrakenOhlcResponse response, Instant endTimeCursor) {
        JsonNode resultNode = response.result();
        JsonNode klinesArray = null;

        for (Map.Entry<String, JsonNode> field : resultNode.properties()) {
            if (!field.getKey().equals("last")) {
                klinesArray = field.getValue();
            }
        }

        if (klinesArray == null || !klinesArray.isArray()) {
            return List.of();
        }

        List<HistoricalPriceDto> klines = new ArrayList<>();
        for (JsonNode kline : klinesArray) {
            long timeInSeconds = kline.get(0).asLong();
            Instant openTime = Instant.ofEpochSecond(timeInSeconds);

            if (openTime.isAfter(endTimeCursor)) {
                continue;
            }
            klines.add(HistoricalPriceDto.builder()
                    .openTime(openTime)
                    .open(new BigDecimal(kline.get(1).asString()))
                    .high(new BigDecimal(kline.get(2).asString()))
                    .low(new BigDecimal(kline.get(3).asString()))
                    .close(new BigDecimal(kline.get(4).asString()))
                    .volume(new BigDecimal(kline.get(6).asString()))
                    .build());
        }
        return klines;
    }

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

    private TradingPair resolveTradingPair(String rawTradingPair) {
        Map<TradingPair, String> tradingPairMap = properties.tradingPair();

        return tradingPairMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(rawTradingPair))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
