package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpringDataChartWidgetRepository extends CrudRepository<ChartWidgetEntity, UUID> {

    @Query("""
          SELECT COALESCE(MAX(c.position), 0)
          FROM ChartWidgetEntity c
          WHERE c.workspace.id = :workspaceId""")
    int findMaxPositionByWorkspaceId(UUID workspaceId);
}
