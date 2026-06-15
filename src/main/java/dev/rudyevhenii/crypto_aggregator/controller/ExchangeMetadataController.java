package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeMetadataDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.service.ExchangeMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeMetadataController {

    private final ExchangeMetadataService metadataService;

    @GetMapping
    public ResponseEntity<List<Exchange>> getSupportedExchanges() {
        return ResponseEntity.ok(metadataService.getSupportedExchanges());
    }

    @GetMapping("/{exchange}/pairs")
    public ResponseEntity<List<TradingPair>> getSupportedPairs(@PathVariable Exchange exchange) {
        return ResponseEntity.ok(metadataService.getSupportedPairs(exchange));
    }

    @GetMapping("/{exchange}/intervals")
    public ResponseEntity<List<ChartInterval>> getSupportedIntervals(@PathVariable Exchange exchange) {
        return ResponseEntity.ok(metadataService.getSupportedIntervals(exchange));
    }

    @GetMapping("/metadata")
    public ResponseEntity<List<ExchangeMetadataDto>> getAllMetadata() {
        return ResponseEntity.ok(metadataService.getAllMetadata());
    }
}
