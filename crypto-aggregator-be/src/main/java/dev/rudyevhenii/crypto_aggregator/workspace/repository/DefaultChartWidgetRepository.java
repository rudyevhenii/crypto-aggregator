package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.ChartWidgetEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultChartWidgetRepository implements ChartWidgetRepository {

    private final SpringDataChartWidgetRepository repository;
    private final ChartWidgetEntityMapper mapper;

    @Override
    public ChartWidget create(UUID workspaceId, ChartWidget chartWidget) {
        ChartWidgetEntity createEntity = mapper.toCreateEntity(chartWidget, workspaceId);
        ChartWidgetEntity savedEntity = repository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<ChartWidget> findAllByWorkspaceId(UUID workspaceId) {
        return repository.findAllByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public int findMaxPositionByWorkspaceId(UUID workspaceId) {
        return repository.findMaxPositionByWorkspaceId(workspaceId);
    }
}
