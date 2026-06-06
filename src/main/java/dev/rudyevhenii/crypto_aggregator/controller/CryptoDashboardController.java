package dev.rudyevhenii.crypto_aggregator.controller;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoDashboardDto;
import dev.rudyevhenii.crypto_aggregator.service.CryptoDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoDashboardController {

    private final CryptoDashboardService cryptoDashboardService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CryptoDashboardDto> streamCryptoPrices() {
        return cryptoDashboardService.streamCryptoPrices();
    }
}
