package dev.rudyevhenii.crypto_aggregator.chart_widget.dto;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;

public record UpdateChartWidgetRequest(
        ChartInterval chartInterval
) {
}
