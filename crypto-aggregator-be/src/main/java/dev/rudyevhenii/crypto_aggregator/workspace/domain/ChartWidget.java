package dev.rudyevhenii.crypto_aggregator.workspace.domain;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
