package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.CryptoDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoDashboardController {

    private final CryptoDashboardService cryptoDashboardService;

    @GetMapping("/exchanges")
    public ResponseEntity<List<Exchange>> getAvailableExchanges() {
        List<Exchange> exchanges = Arrays.asList(Exchange.values());
        return ResponseEntity.ok(exchanges);
    }

    @GetMapping("/pairs")
    public ResponseEntity<List<TradingPair>> getAvailableTradingPairs() {
        List<TradingPair> tradingPairs = Arrays.asList(TradingPair.values());
        return ResponseEntity.ok(tradingPairs);
    }

    @GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CryptoDashboardDto> streamAllPrices() {
        return cryptoDashboardService.streamAllPrices();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CryptoDashboardDto> streamPricesByPair(@RequestParam("pair") TradingPair tradingPair) {
        return cryptoDashboardService.streamPricesByPair(tradingPair);
    }

    @GetMapping(value = "/stream/exchange/{exchange}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CryptoDashboardDto> streamPricesByExchange(@PathVariable Exchange exchange) {
        return cryptoDashboardService.streamPricesByExchange(exchange);
    }

    @GetMapping(value = "/stream/exchange/{exchange}/pair/{pair}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CryptoDashboardDto> streamSinglePairOnExchange(@PathVariable Exchange exchange,
                                                               @PathVariable("pair") TradingPair tradingPair) {
        return cryptoDashboardService.streamSinglePairOnExchange(exchange, tradingPair);
    }

    @GetMapping("/history/exhange/{exchange}/pair/{pair}")
    public Mono<List<HistoricalPriceDto>> streamHistoricalPricesOnExchange(
            @PathVariable Exchange exchange,
            @PathVariable("pair") TradingPair tradingPair,
            HistoricalPriceRequest request
    ) {
        request.setTradingPair(tradingPair);
        return cryptoDashboardService.getHistoricalPrices(exchange, request);
    }
}
