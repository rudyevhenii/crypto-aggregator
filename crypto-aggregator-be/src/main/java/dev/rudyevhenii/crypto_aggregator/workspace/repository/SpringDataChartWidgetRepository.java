package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataChartWidgetRepository extends CrudRepository<ChartWidgetEntity, UUID> {

    @Query("""
          SELECT COALESCE(MAX(c.position), 1)
          FROM ChartWidgetEntity c
          WHERE c.workspaceId = :workspaceId""")
    int findMaxPositionByWorkspaceId(@Param(ChartWidgetEntity.Fields.workspaceId) UUID workspaceId);

    List<ChartWidgetEntity> findAllByWorkspaceId(UUID workspaceId);
}
