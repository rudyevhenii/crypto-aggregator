package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.LiveExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream/exchanges")
@RequiredArgsConstructor
public class LiveExchangeController {

    private final LiveExchangeService liveExchangeService;

    @GetMapping(value = "/{exchange}/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceDto> streamPriceByExchange(@PathVariable Exchange exchange) {
        return liveExchangeService.streamPriceByExchange(exchange);
    }

    @GetMapping(value = "/{exchange}/prices/{pair}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceDto> streamPricesByPair(@PathVariable Exchange exchange, @PathVariable TradingPair pair) {
        return liveExchangeService.streamSinglePair(exchange, pair);
    }

    @GetMapping(value = "/{exchange}/health", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExchangeHealthDto> streamPricesByExchange(@PathVariable Exchange exchange) {
        return liveExchangeService.streamExchangeHealth(exchange);
    }
}
