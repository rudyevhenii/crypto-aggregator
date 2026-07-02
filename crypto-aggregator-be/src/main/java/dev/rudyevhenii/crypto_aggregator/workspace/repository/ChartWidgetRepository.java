package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;

import java.util.UUID;

public interface ChartWidgetRepository {

    ChartWidget create(UUID workspaceId, ChartWidget chartWidget);

    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
