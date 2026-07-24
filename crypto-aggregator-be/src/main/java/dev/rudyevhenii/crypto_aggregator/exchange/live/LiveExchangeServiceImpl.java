package dev.rudyevhenii.crypto_aggregator.exchange.live;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.strategy.LiveExchangeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveExchangeServiceImpl implements LiveExchangeService {

    private static final int BUFFER_DELAY_MILLIS = 500;

    private final Map<Exchange, LiveExchangeStrategy> liveExchangeStrategies;

    @Override
    public Flux<List<LivePriceDto>> streamAllPrices() {
        return Flux.merge(liveExchangeStrategies.entrySet().stream()
                        .map(entry -> entry.getValue().streamPriceByExchange(entry.getKey()))
                        .toList())
                .buffer(Duration.ofMillis(BUFFER_DELAY_MILLIS))
                .filter(list -> !list.isEmpty())
                .map(bufferedTicks -> bufferedTicks.stream()
                        .collect(Collectors.groupingBy(dto -> Tuples.of(dto.exchange(), dto.tradingPair())))
                        .values().stream()
                        .map(this::aggregateTicks)
                        .toList()
                );
    }

    @Override
    public Flux<LivePriceDto> streamPriceByExchange(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamPriceByExchange(exchange);
    }

    @Override
    public Flux<LivePriceDto> streamSinglePair(Exchange exchange, TradingPair pair) {
        return liveExchangeStrategies.get(exchange)
                .streamSinglePair(exchange, pair);
    }

    @Override
    public Flux<ExchangeHealthDto> streamExchangeHealth(Exchange exchange) {
        return liveExchangeStrategies.get(exchange)
                .streamExchangeHealth(exchange);
    }

    private LivePriceDto aggregateTicks(List<LivePriceDto> ticks) {
        LivePriceDto lastTick = ticks.get(ticks.size() - 1);
        BigDecimal maxHigh24h = determineMaxHigh24h(ticks, lastTick);
        BigDecimal minLow24h = determineMinLow24h(ticks, lastTick);

        return LivePriceDto.builder()
                .exchange(lastTick.exchange())
                .tradingPair(lastTick.tradingPair())
                .lastPrice(lastTick.lastPrice())
                .priceChangePercent24h(lastTick.priceChangePercent24h())
                .highPrice24h(maxHigh24h)
                .lowPrice24h(minLow24h)
                .volume24h(lastTick.volume24h())
                .timestamp(lastTick.timestamp())
                .build();
    }

    private BigDecimal determineMaxHigh24h(List<LivePriceDto> ticks, LivePriceDto lastTick) {
        return ticks.stream()
                .map(LivePriceDto::highPrice24h)
                .max(BigDecimal::compareTo)
                .orElse(lastTick.highPrice24h());
    }

    private BigDecimal determineMinLow24h(List<LivePriceDto> ticks, LivePriceDto lastTick) {
        return ticks.stream()
                .map(LivePriceDto::lowPrice24h)
                .min(BigDecimal::compareTo)
                .orElse(lastTick.lowPrice24h());
    }
}
