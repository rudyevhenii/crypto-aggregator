package dev.rudyevhenii.crypto_aggregator.exchange.metadata;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.metadata.model.ExchangeMetadataDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.BinanceProperties;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.CoinbaseProperties;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.KrakenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeMetadataServiceImpl implements ExchangeMetadataService {

    private final BinanceProperties binanceProperties;
    private final CoinbaseProperties coinbaseProperties;
    private final KrakenProperties krakenProperties;

    @Override
    public List<Exchange> getSupportedExchanges() {
        return List.of(Exchange.values());
    }

    @Override
    public List<TradingPair> getSupportedPairs(Exchange exchange) {
        return switch (exchange) {
            case BINANCE -> new ArrayList<>(binanceProperties.tradingPair().keySet());
            case KRAKEN -> new ArrayList<>(krakenProperties.tradingPair().keySet());
            case COINBASE -> new ArrayList<>(coinbaseProperties.tradingPair().keySet());
        };
    }

    @Override
    public List<ChartInterval> getSupportedIntervals(Exchange exchange) {
        return switch (exchange) {
            case BINANCE -> new ArrayList<>(binanceProperties.chartInterval().keySet());
            case KRAKEN -> new ArrayList<>(krakenProperties.chartInterval().keySet());
            case COINBASE -> new ArrayList<>(coinbaseProperties.chartInterval().keySet());
        };
    }

    @Override
    public List<ExchangeMetadataDto> getAllMetadata() {
        return getSupportedExchanges().stream()
                .map(exchange -> ExchangeMetadataDto.builder()
                        .exchange(exchange)
                        .supportedPairs(getSupportedPairs(exchange))
                        .supportedIntervals(getSupportedIntervals(exchange))
                        .build())
                .toList();
    }
}
