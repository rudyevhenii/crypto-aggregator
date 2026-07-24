package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.ChartWidgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChartWidgetServiceImpl implements ChartWidgetService {

    private final ChartWidgetRepository repository;
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public ChartWidget create(UUID workspaceId, ChartWidgetRequest request) {
        int nextPosition = repository.findMaxPositionByWorkspaceId(workspaceId) + 1;
        ChartWidget chartWidget = toDomain(nextPosition, workspaceId, request.exchangePairId());

        log.info("User [{}] created chart widget for workspace [{}]", userContext.getUserId(), workspaceId);
        return repository.create(chartWidget);
    }

    @Override
    @Transactional
    public ChartWidget update(UUID workspaceId, UUID id, UpdateChartWidgetRequest request) {
        ChartWidget chartWidget = getById(workspaceId, id);
        chartWidget.setUpdatedAt(generator.now());

        log.info("User [{}] updated chart widget [{}] for workspace [{}]", userContext.getUserId(), id, workspaceId);
        return repository.update(chartWidget);
    }

    @Override
    @Transactional
    public void updatePositions(UUID workspaceId,
                                List<UpdateChartWidgetPositionsRequest> requests) {
        Map<UUID, ChartWidget> chartWidgetMap = repository.findAllByWorkspaceId(workspaceId).stream()
                .collect(Collectors.toMap(ChartWidget::getId, Function.identity()));

        List<ChartWidget> chartWidgets = new ArrayList<>();
        for (UpdateChartWidgetPositionsRequest request : requests) {
            ChartWidget chartWidget = chartWidgetMap.get(request.chartWidgetId());
            if (chartWidget != null) {
                updateChartWidgetPositions(request, chartWidget);
                chartWidgets.add(chartWidget);
            }
        }
        if (!CollectionUtils.isEmpty(chartWidgets)) {
            repository.updatePositions(workspaceId, chartWidgets);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartWidget> getAllByWorkspaceId(UUID workspaceId) {
        return repository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void delete(UUID workspaceId, UUID id) {
        log.info("User [{}] deleted chart widget [{}] from workspace [{}]", userContext.getUserId(), id, workspaceId);
        repository.deleteById(workspaceId, id);
    }

    private ChartWidget getById(UUID workspaceId, UUID id) {
        return repository.findByWorkspaceIdAndId(workspaceId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart widget not found with id: '%s'".formatted(id)));
    }

    private void updateChartWidgetPositions(UpdateChartWidgetPositionsRequest request,
                                            ChartWidget chartWidget) {
        log.info("Updating chart widget [{}] positions: [{}] => [{}]", chartWidget.getId(),
                chartWidget.getPosition(), request.position());
        chartWidget.setPosition(request.position());
        chartWidget.setUpdatedAt(generator.now());
    }

    private ChartWidget toDomain(int position, UUID workspaceId, UUID exchangePairId) {
        return ChartWidget.builder()
                .id(generator.uuid())
                .exchangePairId(exchangePairId)
                .workspaceId(workspaceId)
                .position(position)
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
