package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalPriceRequest {
    private Exchange exchange;
    private TradingPair tradingPair;
    private ChartInterval interval;
    @Builder.Default
    private Integer limit = 50;
    private Instant endTimeCursor;

    public Instant resolveEndTimeCursor() {
        return getEndTimeCursor() == null
                ? Instant.now()
                : getEndTimeCursor().minusMillis(1);
    }
}
