package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
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

    @GetMapping("/{exchange}/klines")
    public Mono<List<HistoricalPriceDto>> getHistoricalPrices(@PathVariable Exchange exchange, HistoricalPriceRequest request) {
        return historicalExchangeService.getHistoricalPrices(exchange, request);
    }

    @GetMapping("/{exchange}/tickers/24h")
    public Mono<List<Ticker24hDto>> get24hTickersByExchange(@PathVariable Exchange exchange) {
        return historicalExchangeService.get24hTickersByExchange(exchange);
    }

    @GetMapping("/{exchange}/tickers/24h/{pair}")
    public Mono<Ticker24hDto> get24hTickerForPair(@PathVariable Exchange exchange, @PathVariable TradingPair pair) {
        return historicalExchangeService.get24hTickerForPair(exchange, pair);
    }
}
