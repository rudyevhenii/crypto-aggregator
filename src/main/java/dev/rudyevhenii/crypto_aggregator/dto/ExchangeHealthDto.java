package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.ConnectionStatus;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ExchangeHealthDto(
        Exchange exchange,
        ConnectionStatus connectionStatus,
        Instant timestamp
) {
}
