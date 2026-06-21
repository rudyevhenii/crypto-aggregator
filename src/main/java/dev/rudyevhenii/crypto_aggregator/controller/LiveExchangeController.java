package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.mapper.ExchangeMapper;
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
    private final ExchangeMapper mapper;

    @GetMapping(value = "/{exchange}/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceRqDto> streamPriceByExchange(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        return liveExchangeService.streamPriceByExchange(exchangeDomain)
                .map(mapper::toResponse);
    }

    @GetMapping(value = "/{exchange}/prices/{pair}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceRqDto> streamSinglePair(@PathVariable ExchangeRqDto exchange,
                                                 @PathVariable TradingPairRqDto pair) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        TradingPair tradingPairDomain = mapper.toDomain(pair);
        return liveExchangeService.streamSinglePair(exchangeDomain, tradingPairDomain)
                .map(mapper::toResponse);
    }

    @GetMapping(value = "/{exchange}/health", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExchangeHealthRqDto> streamExchangeHealth(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.toDomain(exchange);
        return liveExchangeService.streamExchangeHealth(exchangeDomain)
                .map(mapper::toResponse);
    }
}
