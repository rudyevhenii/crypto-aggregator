package dev.rudyevhenii.crypto_aggregator.exchange.live;

import dev.rudyevhenii.crypto_aggregator.api.dto.live.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.mapper.ExchangeLiveMapper;
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
    private final ExchangeLiveMapper mapper;

    @GetMapping(value = "/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceRqDto> streamAllPrices() {
        return liveExchangeService.streamAllPrices()
                .map(mapper::map);
    }

    @GetMapping(value = "/{exchange}/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceRqDto> streamPriceByExchange(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.map(exchange);
        return liveExchangeService.streamPriceByExchange(exchangeDomain)
                .map(mapper::map);
    }

    @GetMapping(value = "/{exchange}/prices/{pair}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<LivePriceRqDto> streamSinglePair(@PathVariable ExchangeRqDto exchange,
                                                 @PathVariable TradingPairRqDto pair) {
        Exchange exchangeDomain = mapper.map(exchange);
        TradingPair tradingPairDomain = mapper.map(pair);
        return liveExchangeService.streamSinglePair(exchangeDomain, tradingPairDomain)
                .map(mapper::map);
    }

    @GetMapping(value = "/{exchange}/health", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExchangeHealthRqDto> streamExchangeHealth(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.map(exchange);
        return liveExchangeService.streamExchangeHealth(exchangeDomain)
                .map(mapper::map);
    }
}
