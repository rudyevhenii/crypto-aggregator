package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.service.ExchangePairService;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.ChartWidgetRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChartWidgetServiceImpl implements ChartWidgetService {

    private static final int NEXT_POSITION = 1;

    private final ChartWidgetRepository chartWidgetRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ExchangePairService exchangePairService;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public ChartWidget create(UUID userId, UUID workspaceId, ChartWidgetRequest request) {
        validateWorkspaceExists(userId, workspaceId);
        validateExchangePairExists(request.exchangePairId());

        int position = chartWidgetRepository.findMaxPositionByWorkspaceId(workspaceId) + NEXT_POSITION;
        ChartWidget chartWidget = toDomain(position, request.exchangePairId());

        return chartWidgetRepository.create(workspaceId, chartWidget);
    }

    private void validateWorkspaceExists(UUID userId, UUID workspaceId) {
        if (!workspaceRepository.existsById(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: %s"
                    .formatted(workspaceId));
        }
    }

    private void validateExchangePairExists(UUID exchangePairId) {
        if (!exchangePairService.existsById(exchangePairId)) {
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
