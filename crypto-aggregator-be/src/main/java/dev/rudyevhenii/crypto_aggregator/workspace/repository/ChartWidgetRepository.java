package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;

import java.util.List;
import java.util.UUID;

public interface ChartWidgetRepository {

    ChartWidget create(UUID workspaceId, ChartWidget chartWidget);

    List<ChartWidget> findAllByWorkspaceId(UUID workspaceId);

    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
