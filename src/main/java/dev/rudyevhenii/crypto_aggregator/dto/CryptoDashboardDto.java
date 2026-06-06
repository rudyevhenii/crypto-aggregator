package dev.rudyevhenii.crypto_aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CryptoDashboardDto {
    private List<CryptoPriceDto> cryptoPrices;
    private Instant updateTime;
}
