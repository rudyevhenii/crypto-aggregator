package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.ChartWidgetEntityMapper;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.ChartWidgetRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartWidgetServiceImpl implements ChartWidgetService {

    private static final int NEXT_POSITION = 1;

    private final ChartWidgetRepository chartWidgetRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ExchangePairRepository exchangePairRepository;
    private final GeneratorUtils generator;
    private final ChartWidgetEntityMapper mapper;

    @Override
    @Transactional
    public ChartWidget create(UUID userId, UUID workspaceId, ChartWidgetRequest request) {
        validateWorkspaceExists(userId, workspaceId);
        validateExchangePairExists(request.exchangePairId());

        int position = chartWidgetRepository.findMaxPositionByWorkspaceId(workspaceId) + NEXT_POSITION;
        ChartWidget chartWidget = toDomain(position, request.exchangePairId());

        ChartWidgetEntity chartWidgetEntity = mapper.toCreateEntity(chartWidget);
        chartWidgetEntity.setExchangePair(exchangePairRepository.getReferenceById(request.exchangePairId()));
        WorkspaceEntity workspaceEntity = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: %s".formatted(workspaceId)));
        workspaceEntity.addChartWidget(chartWidgetEntity);

        return mapper.toDomain(chartWidgetEntity);
    }

    @Override
    @Transactional
    public ChartWidget update(UUID userId, UUID workspaceId, UUID chartWidgetId, UpdateChartWidgetRequest request) {
        validateWorkspaceExists(userId, workspaceId);
        ChartWidgetEntity chartWidgetEntity = chartWidgetRepository.findById(chartWidgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Chart widget not found with id: %s".formatted(chartWidgetId)));
        updateChartWidget(request, chartWidgetEntity);
        return mapper.toDomain(chartWidgetEntity);
    }

    @Override
    @Transactional
    public void updateChartWidgetPositions(UUID userId, UUID workspaceId,
                                           List<UpdateChartWidgetPositionsRequest> requestList) {
        validateWorkspaceExists(userId, workspaceId);
        Map<UUID, ChartWidgetEntity> chartWidgetMap = chartWidgetRepository.findAllByWorkspaceId(workspaceId).stream()
                .collect(Collectors.toMap(ChartWidgetEntity::getId, Function.identity()));

        for (UpdateChartWidgetPositionsRequest request : requestList) {
            ChartWidgetEntity chartWidgetEntity = chartWidgetMap.get(request.chartWidgetId());
            if (chartWidgetEntity != null) {
                updateChartWidgetPositions(request, chartWidgetEntity);
            }
        }
    }

    private void updateChartWidgetPositions(UpdateChartWidgetPositionsRequest request,
                                            ChartWidgetEntity chartWidgetEntity) {
        chartWidgetEntity.setPosition(request.position());
        chartWidgetEntity.setUpdatedAt(generator.now());
    }

    private void updateChartWidget(UpdateChartWidgetRequest request, ChartWidgetEntity chartWidgetEntity) {
        mapper.toUpdateEntity(request, chartWidgetEntity);
        chartWidgetEntity.setUpdatedAt(generator.now());
    }

    private void validateWorkspaceExists(UUID userId, UUID workspaceId) {
        if (!workspaceRepository.existsByUserIdAndId(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: %s"
                    .formatted(workspaceId));
        }
    }

    private void validateExchangePairExists(UUID exchangePairId) {
        if (!exchangePairRepository.existsById(exchangePairId)) {
            throw new ResourceNotFoundException("Exchange pair not found with exchangePairId: %s"
                    .formatted(exchangePairId));
        }
    }

    private ChartWidget toDomain(int position, UUID exchangePairId) {
        return ChartWidget.builder()
                .id(generator.uuid())
                .exchangePairId(exchangePairId)
                .position(position)
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
