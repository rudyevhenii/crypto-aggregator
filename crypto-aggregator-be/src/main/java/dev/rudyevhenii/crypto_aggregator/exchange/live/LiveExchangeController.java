package dev.rudyevhenii.crypto_aggregator.exchange.live;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.mapper.ExchangeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/stream/exchanges")
@RequiredArgsConstructor
public class LiveExchangeController {

    private final LiveExchangeService liveExchangeService;
    private final ExchangeMapper mapper;

    @GetMapping(value = "/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<LivePriceRqDto>> streamAllPrices() {
        return liveExchangeService.streamAllPrices()
                .map(list -> list.stream()
                        .map(mapper::toResponse)
                        .toList());
    }

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
