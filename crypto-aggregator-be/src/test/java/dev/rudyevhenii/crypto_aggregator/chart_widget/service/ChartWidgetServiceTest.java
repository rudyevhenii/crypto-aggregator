package dev.rudyevhenii.crypto_aggregator.chart_widget.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.repository.ChartWidgetRepository;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.chart_widget.service.ChartWidgetServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartWidgetServiceTest {

    @Mock
    private ChartWidgetRepository repository;

    @Mock
    private UserContext userContext;

    @Mock
    private GeneratorUtils generator;

    @InjectMocks
    private ChartWidgetServiceImpl service;

    @Test
    void givenChartWidgetRequest_create_shouldCreateChartWidget() {
        when(repository.findMaxPositionByWorkspaceId(WORKSPACE_ID)).thenReturn(POSITION_0);
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.uuid()).thenReturn(CHART_WIDGET_ID_1);
        when(generator.now()).thenReturn(CREATED_AT, CREATED_AT);
        when(repository.create(buildChartWidget(ChartInterval.FIFTEEN_MINUTES)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChartWidget result = service.create(WORKSPACE_ID, buildChartWidgetRequest());

        assertThat(result).isEqualTo(buildChartWidget(ChartInterval.FIFTEEN_MINUTES));
    }

    @Test
    void givenIdAndUpdateRequest_update_shouldUpdateChartWidget() {
        ChartInterval chartInterval = ChartInterval.THIRTY_MINUTES;
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .thenReturn(Optional.of(buildChartWidget(ChartInterval.FIFTEEN_MINUTES)));
        when(repository.update(buildUpdatedChartWidget(chartInterval)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChartWidget result = service.update(WORKSPACE_ID, CHART_WIDGET_ID_1,
                buildUpdateChartWidgetRequest(chartInterval));

        assertThat(result).isEqualTo(buildUpdatedChartWidget(chartInterval));
    }

    @Test
    void givenIdAndUpdateRequestWithSameChartInterval_update_shouldNotUpdateChartWidget() {
        ChartInterval chartInterval = ChartInterval.FIFTEEN_MINUTES;
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .thenReturn(Optional.of(buildChartWidget(chartInterval)));

        ChartWidget result = service.update(WORKSPACE_ID, CHART_WIDGET_ID_1,
                buildUpdateChartWidgetRequest(chartInterval));

        assertThat(result).isEqualTo(buildChartWidget(chartInterval));
        verify(generator, never()).now();
        verify(repository, never()).update(any(ChartWidget.class));
    }

    @Test
    void givenIdAndUpdateRequestForNonExistentChartWidget_update_shouldThrowException() {
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(WORKSPACE_ID, CHART_WIDGET_ID_1,
                buildUpdateChartWidgetRequest(ChartInterval.FIFTEEN_MINUTES)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(generator, never()).now();
        verify(repository, never()).update(any(ChartWidget.class));
    }

    @Test
    void givenWorkspaceIdAndUpdatePositionsRequest_updatePositions_shouldUpdateChartWidgetPositions() {
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(buildChartWidgetList());

        service.updatePositions(WORKSPACE_ID, buildUpdateChartWidgetPositionsRequestList());

        ArgumentCaptor<List<ChartWidget>> captor = ArgumentCaptor.captor();
        verify(repository).updatePositions(eq(WORKSPACE_ID), captor.capture());

        List<ChartWidget> captured = captor.getValue();
        assertThat(captured)
                .usingRecursiveComparison()
                .isEqualTo(buildExpectedChartWidgetUpdatedPositionsList());
    }

    @Test
    void givenEmptyUpdatePositionsRequest_updatePositions_shouldNotUpdateChartWidgetPositions() {
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(buildChartWidgetList());

        service.updatePositions(WORKSPACE_ID, List.of());

        verify(generator, never()).now();
        verify(repository, never()).updatePositions(eq(WORKSPACE_ID), anyList());
    }

    @Test
    void givenUpdatePositionsRequestWithNonExistentWidget_updatePositions_shouldUpdateOnlyExisting() {
        when(generator.now()).thenReturn(UPDATED_AT);
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(List.of(buildChartWidget()));

        service.updatePositions(WORKSPACE_ID, buildUpdateChartWidgetPositionsRequestList());

        ArgumentCaptor<List<ChartWidget>> captor = ArgumentCaptor.captor();
        verify(repository).updatePositions(eq(WORKSPACE_ID), captor.capture());

        List<ChartWidget> captured = captor.getValue();
        assertThat(captured).containsExactly(
                buildExpectedChartWidgetUpdatedPositions(CHART_WIDGET_ID_1, EXCHANGE_PAIR_ID, POSITION_2));
    }

    @Test
    void givenWorkspaceWithNoChartWidgets_updatePositions_shouldNotUpdateChartWidgetPositions() {
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(List.of());

        service.updatePositions(WORKSPACE_ID, buildUpdateChartWidgetPositionsRequestList());

        verify(generator, never()).now();
        verify(repository, never()).updatePositions(eq(WORKSPACE_ID), anyList());
    }

    @Test
    void givenWorkspaceId_getAllByWorkspaceId_shouldReturnAllChartWidgets() {
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(buildChartWidgetList());

        List<ChartWidget> result = service.getAllByWorkspaceId(WORKSPACE_ID);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(buildChartWidgetList());
    }

    @Test
    void givenWorkspaceId_getAllByWorkspaceId_shouldReturnEmptyList() {
        when(repository.findAllByWorkspaceId(WORKSPACE_ID)).thenReturn(List.of());

        List<ChartWidget> result = service.getAllByWorkspaceId(WORKSPACE_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void givenWorkspaceIdAndId_delete_shouldDeleteChartWidget() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1)).thenReturn(true);

        service.delete(WORKSPACE_ID, CHART_WIDGET_ID_1);

        verify(repository).deleteById(WORKSPACE_ID, CHART_WIDGET_ID_1);
    }

    @Test
    void givenNonExistentWorkspaceIdAndId_delete_shouldThrowException() {
        when(repository.existsByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(WORKSPACE_ID, CHART_WIDGET_ID_1);
    }

    static class TestResources {
        static final UUID CHART_WIDGET_ID_1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        static final UUID EXCHANGE_PAIR_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
        static final int POSITION_1 = 1;

        static final UUID CHART_WIDGET_ID_2 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final UUID EXCHANGE_PAIR_ID_2 = UUID.fromString("31111111-1111-1111-1111-111111111113");
        static final int POSITION_2 = 2;

        static final UUID WORKSPACE_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final int POSITION_0 = 0;

        static ChartWidgetRequest buildChartWidgetRequest() {
            return ChartWidgetRequest.builder()
                    .exchangePairId(EXCHANGE_PAIR_ID)
                    .build();
        }

        static List<ChartWidget> buildChartWidgetList() {
            return List.of(
                    buildChartWidget(),
                    buildSecondChartWidget()
            );
        }

        static ChartWidget buildChartWidget() {
            return buildChartWidget(ChartInterval.FIFTEEN_MINUTES);
        }

        static ChartWidget buildChartWidget(ChartInterval chartInterval) {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID_1)
                    .chartInterval(chartInterval)
                    .exchangePairId(EXCHANGE_PAIR_ID)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_1)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static ChartWidget buildSecondChartWidget() {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID_2)
                    .chartInterval(ChartInterval.FIFTEEN_MINUTES)
                    .exchangePairId(EXCHANGE_PAIR_ID_2)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_2)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static ChartWidget buildUpdatedChartWidget(ChartInterval chartInterval) {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID_1)
                    .chartInterval(chartInterval)
                    .exchangePairId(EXCHANGE_PAIR_ID)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_1)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static List<ChartWidget> buildExpectedChartWidgetUpdatedPositionsList() {
            return List.of(
                buildExpectedChartWidgetUpdatedPositions(CHART_WIDGET_ID_1, EXCHANGE_PAIR_ID, POSITION_2),
                buildExpectedChartWidgetUpdatedPositions(CHART_WIDGET_ID_2, EXCHANGE_PAIR_ID_2, POSITION_1)
            );
        }

        static ChartWidget buildExpectedChartWidgetUpdatedPositions(
                UUID chartWidgetId, UUID exchangePairId, int position
        ) {
            return ChartWidget.builder()
                    .id(chartWidgetId)
                    .chartInterval(ChartInterval.FIFTEEN_MINUTES)
                    .exchangePairId(exchangePairId)
                    .workspaceId(WORKSPACE_ID)
                    .position(position)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static List<UpdateChartWidgetPositionsRequest> buildUpdateChartWidgetPositionsRequestList() {
            return List.of(
                    buildUpdateChartWidgetPositionsRequest(CHART_WIDGET_ID_1, POSITION_2),
                    buildUpdateChartWidgetPositionsRequest(CHART_WIDGET_ID_2, POSITION_1)
            );
        }

        static UpdateChartWidgetPositionsRequest buildUpdateChartWidgetPositionsRequest(
                UUID chartWidgetId, int position
        ) {
            return UpdateChartWidgetPositionsRequest.builder()
                    .chartWidgetId(chartWidgetId)
                    .position(position)
                    .build();
        }

        static UpdateChartWidgetRequest buildUpdateChartWidgetRequest(ChartInterval chartInterval) {
            return UpdateChartWidgetRequest.builder()
                    .chartInterval(chartInterval)
                    .build();
        }
    }
}