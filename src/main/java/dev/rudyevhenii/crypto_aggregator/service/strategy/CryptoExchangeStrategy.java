package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface CryptoExchangeStrategy {

    Mono<CryptoPriceDto> streamPrice(TradingPair tradingPair);

    Mono<List<HistoricalPriceDto>> fetchHistoricalPrices(TradingPair tradingPair, ChartInterval chartInterval,
                                                         Instant startTime, Instant endTime);

    CryptoProperties getCryptoProperties();

    Exchange getExchangeType();
}
