package dev.rudyevhenii.crypto_aggregator.service;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CryptoDashboardService {

    Flux<CryptoDashboardDto> streamAllPrices();

    Flux<CryptoDashboardDto> streamPricesByPair(TradingPair tradingPair);

    Flux<CryptoDashboardDto> streamPricesByExchange(Exchange exchange);

    Flux<CryptoDashboardDto> streamSinglePairOnExchange(Exchange exchange, TradingPair tradingPair);

    Mono<List<HistoricalPriceDto>> getHistoricalPrices(Exchange exchange, HistoricalPriceRequest request);
}
