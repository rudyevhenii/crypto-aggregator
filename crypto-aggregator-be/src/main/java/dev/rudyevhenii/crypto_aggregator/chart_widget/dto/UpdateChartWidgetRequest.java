package dev.rudyevhenii.crypto_aggregator.chart_widget.dto;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import lombok.Builder;

@Builder
public record UpdateChartWidgetRequest(
        ChartInterval chartInterval
) {
}
