package dev.rudyevhenii.crypto_aggregator.workspace.dto;

import java.util.UUID;

public record UpdateChartWidgetPositionsRequest(
        UUID chartWidgetId,
        int position
) {
}
