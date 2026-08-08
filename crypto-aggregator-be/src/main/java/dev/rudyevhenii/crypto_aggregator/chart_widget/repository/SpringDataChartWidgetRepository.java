package dev.rudyevhenii.crypto_aggregator.chart_widget.repository;

import dev.rudyevhenii.crypto_aggregator.chart_widget.ChartWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataChartWidgetRepository extends JpaRepository<ChartWidgetEntity, UUID> {

    List<ChartWidgetEntity> findAllByWorkspaceId(UUID workspaceId);

    @Query("""
            SELECT COALESCE(MAX(c.position), 0)
            FROM ChartWidgetEntity c
            WHERE c.workspaceId = :workspaceId""")
    int findMaxPositionByWorkspaceId(UUID workspaceId);

    Optional<ChartWidgetEntity> findByWorkspaceIdAndId(UUID workspaceId, UUID id);

    boolean existsByWorkspaceIdAndId(UUID workspaceId, UUID id);
}
