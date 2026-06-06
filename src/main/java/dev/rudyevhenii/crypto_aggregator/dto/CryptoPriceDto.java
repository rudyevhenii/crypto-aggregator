package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.Symbol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CryptoPriceDto {
    private Exchange exchange;
    private Symbol symbol;
    private BigDecimal price;
    private Instant timestamp;
}
