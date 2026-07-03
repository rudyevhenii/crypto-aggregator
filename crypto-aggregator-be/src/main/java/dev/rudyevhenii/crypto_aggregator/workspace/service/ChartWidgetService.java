package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetRequest;

import java.util.List;
import java.util.UUID;

public interface ChartWidgetService {

    ChartWidget create(UUID userId, UUID workspaceId, ChartWidgetRequest request);

    ChartWidget update(UUID userId, UUID workspaceId, UUID chartWidgetId, UpdateChartWidgetRequest request);

    void updateChartWidgetPositions(UUID userId, UUID workspaceId, List<UpdateChartWidgetPositionsRequest> request);
}
