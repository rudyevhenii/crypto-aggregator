package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChartWidgetRepository extends JpaRepository<ChartWidgetEntity, UUID> {

    List<ChartWidgetEntity> findAllByWorkspaceId(UUID workspaceId);

    @Query("""
            SELECT COALESCE(MAX(c.position), 0)
            FROM ChartWidgetEntity c
            WHERE c.workspace.id = :workspaceId""")
    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
