package dev.rudyevhenii.crypto_aggregator.chart_widget.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.repository.ChartWidgetRepository;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.exception.UnsupportedIntervalException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.SupportedExchangeIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
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

    private final ChartWidgetRepository chartWidgetRepository;
    private final UserContext userContext;
    private final GeneratorUtils generator;
    private final ExchangePairRepository exchangePairRepository;
    private final Map<Exchange, SupportedExchangeIntervalsStrategy> supportedExchangeIntervalsStrategies;

    @Override
    @Transactional
    public ChartWidget create(UUID workspaceId, ChartWidgetRequest request) {
        int nextPosition = chartWidgetRepository.findMaxPositionByWorkspaceId(workspaceId) + 1;
        ChartWidget chartWidget = toDomain(nextPosition, workspaceId, request.exchangePairId());

        log.info("User [{}] created chart widget for workspace [{}]", userContext.getUserId(), workspaceId);
        return chartWidgetRepository.create(chartWidget);
    }

    @Override
    @Transactional
    public ChartWidget update(UUID workspaceId, UUID id, UpdateChartWidgetRequest request) {
        ChartWidget chartWidget = getById(workspaceId, id);

        if (request.chartInterval() != chartWidget.getChartInterval()) {
            ExchangePair exchangePair = getExchangePair(chartWidget.getExchangePairId());
            validateExchangeIntervalSupport(exchangePair.getExchange(), request.chartInterval());

            chartWidget.setChartInterval(request.chartInterval());
            chartWidget.setUpdatedAt(generator.now());
            log.info("User [{}] updated chart widget [{}] for workspace [{}]", userContext.getUserId(), id, workspaceId);
            return chartWidgetRepository.update(chartWidget);
        }
        return chartWidget;
    }

    @Override
    @Transactional
    public void updatePositions(UUID workspaceId,
                                List<UpdateChartWidgetPositionsRequest> requests) {
        Map<UUID, ChartWidget> chartWidgetMap = chartWidgetRepository.findAllByWorkspaceId(workspaceId).stream()
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
            chartWidgetRepository.updatePositions(workspaceId, chartWidgets);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartWidget> getAllByWorkspaceId(UUID workspaceId) {
        return chartWidgetRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void delete(UUID workspaceId, UUID id) {
        // TODO: default implementation of deleteById method already checks if entity exists
        validateChartWidgetExists(workspaceId, id);
        log.info("User [{}] deleted chart widget [{}] from workspace [{}]", userContext.getUserId(), id, workspaceId);
        chartWidgetRepository.deleteById(workspaceId, id);
    }

    private ChartWidget getById(UUID workspaceId, UUID id) {
        return chartWidgetRepository.findByWorkspaceIdAndId(workspaceId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart widget not found with id: '%s'".formatted(id)));
    }

    private ExchangePair getExchangePair(UUID exchangePairId) {
        return exchangePairRepository.findById(exchangePairId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange pair not found with id: '%s'".formatted(exchangePairId)));
    }

    private void validateExchangeIntervalSupport(Exchange exchange, ChartInterval chartInterval) {
        SupportedExchangeIntervalsStrategy supportedExchangeIntervals = supportedExchangeIntervalsStrategies.get(exchange);
        if (!supportedExchangeIntervals.isSupportedInterval(chartInterval)) {
            throw new UnsupportedIntervalException("Exchange '%s' does not support timeframe '%s'"
                    .formatted(exchange, chartInterval));
        }
    }

    private void validateChartWidgetExists(UUID workspaceId, UUID id) {
        if (!chartWidgetRepository.existsByWorkspaceIdAndId(workspaceId, id)) {
            throw new ResourceNotFoundException("Chart widget not found with id: '%s'".formatted(id));
        }
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
