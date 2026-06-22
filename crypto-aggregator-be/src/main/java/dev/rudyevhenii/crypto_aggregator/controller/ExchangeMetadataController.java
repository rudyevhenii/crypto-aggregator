package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartIntervalRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeMetadataRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.ExchangeMetadataApi;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.mapper.ExchangeMapper;
import dev.rudyevhenii.crypto_aggregator.service.ExchangeMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeMetadataController implements ExchangeMetadataApi {

    private final ExchangeMetadataService metadataService;
    private final ExchangeMapper metadataMapper;

    @Override
    public ResponseEntity<List<ExchangeRqDto>> getSupportedExchanges() {
        return ResponseEntity.ok(metadataService.getSupportedExchanges().stream()
                .map(metadataMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<List<TradingPairRqDto>> getSupportedPairs(ExchangeRqDto exchange) {
        Exchange domain = metadataMapper.toDomain(exchange);
        return ResponseEntity.ok(metadataService.getSupportedPairs(domain).stream()
                .map(metadataMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<List<ChartIntervalRqDto>> getSupportedIntervals(ExchangeRqDto exchange) {
        Exchange domain = metadataMapper.toDomain(exchange);
        return ResponseEntity.ok(metadataService.getSupportedIntervals(domain).stream()
                .map(metadataMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<List<ExchangeMetadataRqDto>> getAllMetadata() {
        return ResponseEntity.ok(metadataService.getAllMetadata().stream()
                .map(metadataMapper::toResponse)
                .toList());
    }
}
