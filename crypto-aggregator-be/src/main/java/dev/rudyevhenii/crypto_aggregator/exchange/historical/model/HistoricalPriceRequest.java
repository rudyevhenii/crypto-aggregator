package dev.rudyevhenii.crypto_aggregator.exchange.historical.model;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalPriceRequest {
    private TradingPair tradingPair;
    private ChartInterval chartInterval;
    private Integer limit;
    private Instant endTimeCursor;

    public Instant resolveEndTimeCursor() {
        return getEndTimeCursor() == null
                ? Instant.now()
                : getEndTimeCursor().minusMillis(1);
    }
}
