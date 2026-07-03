package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ChartWidgetRepository extends JpaRepository<ChartWidgetEntity, UUID> {

    @Query("""
          SELECT COALESCE(MAX(c.position), 0)
          FROM ChartWidgetEntity c
          WHERE c.workspace.id = :workspaceId""")
    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
