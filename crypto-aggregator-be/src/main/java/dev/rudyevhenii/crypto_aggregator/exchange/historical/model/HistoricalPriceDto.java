package dev.rudyevhenii.crypto_aggregator.exchange.historical.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record HistoricalPriceDto(
        Instant openTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
