package dev.rudyevhenii.crypto_aggregator.exchange.live.model;

import dev.rudyevhenii.crypto_aggregator.core.enums.ConnectionStatus;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ExchangeHealthDto(
        Exchange exchange,
        ConnectionStatus connectionStatus,
        Instant timestamp
) {
}
