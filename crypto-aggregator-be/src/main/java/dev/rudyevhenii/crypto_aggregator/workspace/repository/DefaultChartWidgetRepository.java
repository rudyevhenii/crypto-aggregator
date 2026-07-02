package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.SpringDataExchangePairRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.ChartWidgetEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultChartWidgetRepository implements ChartWidgetRepository {

    private final SpringDataChartWidgetRepository chartWidgetRepository;
    private final SpringDataWorkspaceRepository workspaceRepository;
    private final SpringDataExchangePairRepository exchangePairRepository;
    private final ChartWidgetEntityMapper mapper;

    @Override
    public Optional<ChartWidget> findById(UUID chartWidgetId) {
        return chartWidgetRepository.findById(chartWidgetId)
                .map(mapper::toDomain);
    }

    @Override
    public ChartWidget create(UUID workspaceId, ChartWidget chartWidget) {
        ChartWidgetEntity createEntity = mapper.toCreateEntity(chartWidget, workspaceId);
        createEntity.setWorkspace(workspaceRepository.getReferenceById(workspaceId));
        createEntity.setExchangePair(exchangePairRepository.getReferenceById(chartWidget.getExchangePairId()));
        ChartWidgetEntity savedEntity = chartWidgetRepository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public int findMaxPositionByWorkspaceId(UUID workspaceId) {
        return chartWidgetRepository.findMaxPositionByWorkspaceId(workspaceId);
    }

    @Override
    public ChartWidget update(UUID workspaceId, ChartWidget chartWidget) {
        ChartWidgetEntity updateEntity = mapper.toUpdateEntity(chartWidget);
        updateEntity.setWorkspace(workspaceRepository.getReferenceById(workspaceId));
        updateEntity.setExchangePair(exchangePairRepository.getReferenceById(chartWidget.getExchangePairId()));
        ChartWidgetEntity savedEntity = chartWidgetRepository.save(updateEntity);
        return mapper.toDomain(savedEntity);
    }
}
