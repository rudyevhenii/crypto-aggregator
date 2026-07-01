package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.service.ExchangePairService;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.ChartWidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChartWidgetServiceImpl implements ChartWidgetService {

    private static final int NEXT_POSITION = 1;

    private final ChartWidgetRepository chartWidgetRepository;
    private final ExchangePairService exchangePairService;
    private final GeneratorUtils generator;

    @Override
    public ChartWidget create(UUID workspaceId, ChartWidgetRequest request) {
        validateExchangePairExists(request.exchangePairId());

        int position = chartWidgetRepository.findMaxPositionByWorkspaceId(workspaceId) + NEXT_POSITION;
        ExchangePair exchangePair = exchangePairService.getById(request.exchangePairId());
        ChartWidget chartWidget = toDomain(position, exchangePair);

        return chartWidgetRepository.create(workspaceId, chartWidget);
    }

    private void validateExchangePairExists(UUID id) {
        if (exchangePairService.existsById(id)) {
            throw new ResourceNotFoundException("Exchange pair not found with id: %s".formatted(id));
        }
    }

    private ChartWidget toDomain(int position, ExchangePair exchangePair) {
        return ChartWidget.builder()
                .id(generator.uuid())
                .tradingPair(exchangePair.getTradingPair())
                .exchange(exchangePair.getExchange())
                .position(position)
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
