package dev.rudyevhenii.crypto_aggregator.workspace.dto;

import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        List<ChartWidgetResponse> chartWidgetResponses,
        Instant createAt,
        Instant updatedAt
) {
}
