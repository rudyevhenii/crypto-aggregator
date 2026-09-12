package dev.rudyevhenii.crypto_aggregator.exchange.historical;

import dev.rudyevhenii.crypto_aggregator.api.dto.historical.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.historical.HistoricalExchangeApi;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper.ExchangeHistoricalMapper;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistoricalExchangeController implements HistoricalExchangeApi {

    private final HistoricalExchangeService service;
    private final ExchangeHistoricalMapper mapper;

    @Override
    public ResponseEntity<List<HistoricalPriceRqDto>> getHistoricalPrices(ExchangeRqDto exchange,
                                                                          HistoricalPriceRequestRqDto request) {
        Exchange exchangeDomain = mapper.map(exchange);
        HistoricalPriceRequest historicalPriceDomain = mapper.map(request);
        List<HistoricalPriceDto> response = service.getHistoricalPrices(exchangeDomain, historicalPriceDomain);
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<List<Ticker24hRqDto>> get24hTickersByExchange(ExchangeRqDto exchange,
                                                                        List<TradingPairRqDto> tradingPairs) {
        Exchange exchangeDomain = mapper.map(exchange);
        List<TradingPair> tradingPairsDomain = tradingPairs.stream()
                .map(mapper::map)
                .toList();
        List<Ticker24hDto> response = service.get24hTickersByExchange(exchangeDomain, tradingPairsDomain);
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<Ticker24hRqDto> get24hTickerForPair(ExchangeRqDto exchange,
                                                              TradingPairRqDto pair) {
        Exchange exchangeDomain = mapper.map(exchange);
        TradingPair tradingPairDomain = mapper.map(pair);
        Ticker24hDto response = service.get24hTickerForPair(exchangeDomain, tradingPairDomain);
        return ResponseEntity.ok(mapper.map(response));
    }
}
