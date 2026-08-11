package dev.rudyevhenii.crypto_aggregator.chart_widget.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ChartWidgetRequest(
        UUID exchangePairId
) {
}
