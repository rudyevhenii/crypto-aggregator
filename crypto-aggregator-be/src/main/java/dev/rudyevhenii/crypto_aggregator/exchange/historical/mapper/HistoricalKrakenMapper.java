package dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.util.ExchangeUtils;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.KrakenOhlcResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.integration.dto.KrakenTicker24hResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.KrakenProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoricalKrakenMapper {

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
                .build();
    }

    public List<Ticker24hDto> toTickerDtoList(KrakenTicker24hResponse res) {
        if (res == null || res.result() == null || res.result().isEmpty()) {
            return new ArrayList<>();
        }

        return res.result().entrySet().stream()
                .map(entry -> {
                    String rawTradingPair = entry.getKey();
                    KrakenTicker24hResponse.KrakenTickerData tickerData = entry.getValue();

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
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<HistoricalPriceDto> toHistoricalPriceDto(KrakenOhlcResponse response, Instant endTimeCursor) {
        if (response == null || response.result() == null || response.result().isNull()) {
            log.warn("Kraken API returned empty result or error. Response: {}", response);
            return new ArrayList<>();
        }

        JsonNode resultNode = response.result();
        JsonNode klinesArray = null;

        for (Map.Entry<String, JsonNode> field : resultNode.properties()) {
            if (!field.getKey().equals("last")) {
                klinesArray = field.getValue();
            }
        }

        if (klinesArray == null || !klinesArray.isArray()) {
            return new ArrayList<>();
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

    private TradingPair resolveTradingPair(String rawTradingPair) {
        Map<TradingPair, String> tradingPairMap = properties.tradingPair();

        return tradingPairMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(rawTradingPair))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
