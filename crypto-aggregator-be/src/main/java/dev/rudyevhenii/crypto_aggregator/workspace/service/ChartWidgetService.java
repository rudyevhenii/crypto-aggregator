package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetRequest;

import java.util.List;
import java.util.UUID;

public interface ChartWidgetService {

    ChartWidget create(UUID workspaceId, ChartWidgetRequest request);

    ChartWidget update(UUID workspaceId, UUID id, UpdateChartWidgetRequest request);

    void updatePositions(UUID workspaceId, List<UpdateChartWidgetPositionsRequest> request);

    List<ChartWidget> getAllByWorkspaceId(UUID workspaceId);

    void delete(UUID workspaceId, UUID id);
}
