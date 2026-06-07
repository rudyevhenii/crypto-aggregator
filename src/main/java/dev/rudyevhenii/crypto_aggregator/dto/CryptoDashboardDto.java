package dev.rudyevhenii.crypto_aggregator.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record CryptoDashboardDto(
        List<CryptoPriceDto> cryptoPrices,
        Instant updateTime
) {
}
