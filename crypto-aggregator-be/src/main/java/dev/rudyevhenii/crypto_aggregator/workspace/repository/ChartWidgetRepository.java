package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChartWidgetRepository {

    ChartWidget create(ChartWidget chartWidget);

    ChartWidget update(ChartWidget chartWidget);

    void updatePositions(UUID workspaceId, List<ChartWidget> chartWidgets);

    Optional<ChartWidget> findByWorkspaceIdAndId(UUID workspaceId, UUID id);

    List<ChartWidget> findAllByWorkspaceId(UUID workspaceId);

    void deleteById(UUID workspaceId, UUID id);

    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
