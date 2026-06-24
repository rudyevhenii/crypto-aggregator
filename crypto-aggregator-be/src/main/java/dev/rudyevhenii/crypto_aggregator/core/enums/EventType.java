package dev.rudyevhenii.crypto_aggregator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    BINANCE("24hrTicker"),
    COINBASE("ticker"),
    KRAKEN("ticker");

    private final String eventType;
}
