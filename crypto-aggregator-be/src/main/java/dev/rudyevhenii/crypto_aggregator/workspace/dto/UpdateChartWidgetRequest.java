package dev.rudyevhenii.crypto_aggregator.workspace.dto;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;

public record UpdateChartWidgetRequest(
        ChartInterval chartInterval
) {
}
