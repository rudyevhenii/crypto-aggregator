package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.mapper.ExchangeMapper;
import dev.rudyevhenii.crypto_aggregator.service.HistoricalExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/historical/exchanges")
@RequiredArgsConstructor
public class HistoricalExchangeController {

    private final HistoricalExchangeService historicalExchangeService;
    private final ExchangeMapper mapper;

    @GetMapping("/{exchange}/klines")
    public Mono<List<HistoricalPriceRqDto>> getHistoricalPrices(@PathVariable ExchangeRqDto exchange,
                                                                HistoricalPriceRequestRqDto request) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        HistoricalPriceRequest historicalPriceDomain = mapper.toDomain(request);
        return historicalExchangeService.getHistoricalPrices(exchangeDomain, historicalPriceDomain)
                .map(res -> res.stream()
                        .map(mapper::toResponse)
                        .toList());
    }

    @GetMapping("/{exchange}/tickers/24h")
    public Mono<List<Ticker24hRqDto>> get24hTickersByExchange(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        return historicalExchangeService.get24hTickersByExchange(exchangeDomain)
                .map(res -> res.stream()
                        .map(mapper::toResponse)
                        .toList());
    }

    @GetMapping("/{exchange}/tickers/24h/{pair}")
    public Mono<Ticker24hRqDto> get24hTickerForPair(@PathVariable ExchangeRqDto exchange,
                                                    @PathVariable TradingPairRqDto pair) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        TradingPair tradingPairDomain = mapper.toDomain(pair);
        return historicalExchangeService.get24hTickerForPair(exchangeDomain, tradingPairDomain)
                .map(mapper::toResponse);
    }
}
