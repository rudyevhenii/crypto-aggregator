package dev.rudyevhenii.crypto_aggregator.exchange.historical;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.mapper.ExchangeMapper;
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
        Exchange exchangeDomain = mapper.map(exchange);
        HistoricalPriceRequest historicalPriceDomain = mapper.map(request);
        return historicalExchangeService.getHistoricalPrices(exchangeDomain, historicalPriceDomain)
                .map(res -> res.stream()
                        .map(mapper::map)
                        .toList());
    }

    @GetMapping("/{exchange}/tickers/24h")
    public Mono<List<Ticker24hRqDto>> get24hTickersByExchange(@PathVariable ExchangeRqDto exchange) {
        Exchange exchangeDomain = mapper.map(exchange);
        return historicalExchangeService.get24hTickersByExchange(exchangeDomain)
                .map(res -> res.stream()
                        .map(mapper::map)
                        .toList());
    }

    @GetMapping("/{exchange}/tickers/24h/{pair}")
    public Mono<Ticker24hRqDto> get24hTickerForPair(@PathVariable ExchangeRqDto exchange,
                                                    @PathVariable TradingPairRqDto pair) {
        Exchange exchangeDomain = mapper.map(exchange);
        TradingPair tradingPairDomain = mapper.map(pair);
        return historicalExchangeService.get24hTickerForPair(exchangeDomain, tradingPairDomain)
                .map(mapper::map);
    }
}
