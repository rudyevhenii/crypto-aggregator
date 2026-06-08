package dev.rudyevhenii.crypto_aggregator.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record HistoricalPriceDto(
        Instant openTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
