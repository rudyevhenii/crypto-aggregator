package dev.rudyevhenii.crypto_aggregator.dto;

import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

    @NotNull(message = "Trading pair is mandatory")
    private TradingPair tradingPair;

    @NotNull(message = "Chart interval is mandatory")
    private ChartInterval chartInterval;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 200, message = "Limit cannot exceed 200 exchanges limit")
    private Integer limit;

    @PastOrPresent(message = "End time cursor cannot be in the future")
    private Instant endTimeCursor;

    public Instant resolveEndTimeCursor() {
        return getEndTimeCursor() == null
                ? Instant.now()
                : getEndTimeCursor().minusMillis(1);
    }
}
