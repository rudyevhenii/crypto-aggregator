package dev.rudyevhenii.crypto_aggregator.chart_widget.dto;

import java.util.UUID;

public record UpdateChartWidgetPositionsRequest(
        UUID chartWidgetId,
        int position
) {
}
