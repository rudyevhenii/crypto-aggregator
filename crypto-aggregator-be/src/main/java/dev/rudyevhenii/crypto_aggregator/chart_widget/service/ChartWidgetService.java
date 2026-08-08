package dev.rudyevhenii.crypto_aggregator.chart_widget.service;

import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetRequest;

import java.util.List;
import java.util.UUID;

public interface ChartWidgetService {

    ChartWidget create(UUID workspaceId, ChartWidgetRequest request);

    ChartWidget update(UUID workspaceId, UUID id, UpdateChartWidgetRequest request);

    void updatePositions(UUID workspaceId, List<UpdateChartWidgetPositionsRequest> request);

    List<ChartWidget> getAllByWorkspaceId(UUID workspaceId);

    void delete(UUID workspaceId, UUID id);
}
