package dev.rudyevhenii.crypto_aggregator.exchange.metadata;

import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ChartIntervalRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ExchangeMetadataRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.metadata.ExchangeMetadataApi;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange.metadata.mapper.ExchangeMetadataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeMetadataController implements ExchangeMetadataApi {

    private final ExchangeMetadataService service;
    private final ExchangeMetadataMapper mapper;

    // TODO: delete unused endpoints
    @Override
    public ResponseEntity<List<ExchangeRqDto>> getSupportedExchanges() {
        return ResponseEntity.ok(service.getSupportedExchanges().stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<List<TradingPairRqDto>> getSupportedPairs(ExchangeRqDto exchange) {
        Exchange domain = mapper.map(exchange);
        return ResponseEntity.ok(service.getSupportedPairs(domain).stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<List<ChartIntervalRqDto>> getSupportedIntervals(ExchangeRqDto exchange) {
        Exchange domain = mapper.map(exchange);
        return ResponseEntity.ok(service.getSupportedIntervals(domain).stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<List<ExchangeMetadataRqDto>> getAllMetadata() {
        return ResponseEntity.ok(service.getAllMetadata().stream()
                .map(mapper::map)
                .toList());
    }
}
