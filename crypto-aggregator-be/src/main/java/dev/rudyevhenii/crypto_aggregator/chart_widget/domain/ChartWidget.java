package dev.rudyevhenii.crypto_aggregator.chart_widget.domain;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartWidget {
    private UUID id;
    @Builder.Default
    private ChartInterval chartInterval = ChartInterval.FIFTEEN_MINUTES;
    private UUID exchangePairId;
    private UUID workspaceId;
    private int position;
    private Instant createdAt;
    private Instant updatedAt;
}
