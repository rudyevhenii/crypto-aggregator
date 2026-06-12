package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HistoricalExchangeStrategy extends ExchangeStrategy {

    Mono<List<HistoricalPriceDto>> fetchHistoricalData(HistoricalPriceRequest request);

}
