package dev.rudyevhenii.crypto_aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceResponse(BigDecimal price) {
}
