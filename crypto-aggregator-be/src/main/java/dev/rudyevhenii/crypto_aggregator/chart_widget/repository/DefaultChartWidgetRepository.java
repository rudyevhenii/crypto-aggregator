package dev.rudyevhenii.crypto_aggregator.chart_widget.repository;

import dev.rudyevhenii.crypto_aggregator.chart_widget.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.mapper.ChartWidgetEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.CHART_WIDGET_CACHE;

@Repository
@RequiredArgsConstructor
public class DefaultChartWidgetRepository implements ChartWidgetRepository {

    private final SpringDataChartWidgetRepository repository;
    private final ChartWidgetEntityMapper mapper;

    @Override
    @CacheEvict(value = CHART_WIDGET_CACHE, key = "#chartWidget.workspaceId")
    public ChartWidget create(ChartWidget chartWidget) {
        ChartWidgetEntity entity = mapper.toCreateEntity(chartWidget);
        ChartWidgetEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CHART_WIDGET_CACHE, key = "#chartWidget.workspaceId"),
            @CacheEvict(value = CHART_WIDGET_CACHE, key = "#chartWidget.id")
    })
    public ChartWidget update(ChartWidget chartWidget) {
        ChartWidgetEntity entity = mapper.toUpdateEntity(chartWidget);
        ChartWidgetEntity updatedEntity = repository.save(entity);
        return mapper.toDomain(updatedEntity);
    }

    @Override
    @CacheEvict(value = CHART_WIDGET_CACHE, key = "#workspaceId")
    public void updatePositions(UUID workspaceId, List<ChartWidget> chartWidgets) {
        List<ChartWidgetEntity> chartWidgetEntities = chartWidgets.stream()
                .map(mapper::toUpdateEntity)
                .toList();
        repository.saveAll(chartWidgetEntities);
    }

    @Override
    @Cacheable(value = CHART_WIDGET_CACHE, key = "#id", unless = "#result == null")
    public Optional<ChartWidget> findByWorkspaceIdAndId(UUID workspaceId, UUID id) {
        return repository.findByWorkspaceIdAndId(workspaceId, id)
                .map(mapper::toDomain);
    }

    @Override
    @Cacheable(value = CHART_WIDGET_CACHE, key = "#workspaceId")
    public List<ChartWidget> findAllByWorkspaceId(UUID workspaceId) {
        return repository.findAllByWorkspaceId(workspaceId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CHART_WIDGET_CACHE, key = "#id"),
            @CacheEvict(value = CHART_WIDGET_CACHE, key = "#workspaceId")
    })
    public void deleteById(UUID workspaceId, UUID id) {
        repository.deleteById(id);
    }

    @Override
    public int findMaxPositionByWorkspaceId(UUID workspaceId) {
        return repository.findMaxPositionByWorkspaceId(workspaceId);
    }

    @Override
    public boolean existsByWorkspaceIdAndId(UUID workspaceId, UUID id) {
        return repository.existsByWorkspaceIdAndId(workspaceId, id);
    }
}
