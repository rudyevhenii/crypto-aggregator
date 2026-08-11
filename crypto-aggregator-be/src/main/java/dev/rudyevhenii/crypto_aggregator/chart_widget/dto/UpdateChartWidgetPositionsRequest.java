package dev.rudyevhenii.crypto_aggregator.chart_widget.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateChartWidgetPositionsRequest(
        UUID chartWidgetId,
        int position
) {
}
