package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;

import java.util.Optional;
import java.util.UUID;

public interface ChartWidgetRepository {

    Optional<ChartWidget> findById(UUID chartWidgetId);

    ChartWidget create(UUID workspaceId, ChartWidget chartWidget);

    int findMaxPositionByWorkspaceId(UUID workspaceId);

    ChartWidget update(UUID workspaceId, ChartWidget chartWidget);
}
