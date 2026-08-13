package dev.rudyevhenii.crypto_aggregator.chart_widget.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.repository.ChartWidgetRepository;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.exception.UnsupportedIntervalException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.BinanceSupportedIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.CoinbaseSupportedIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.KrakenSupportedIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange.intervals.support.SupportedExchangeIntervalsStrategy;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private ExchangePairRepository exchangePairRepository;

    @Spy
    private BinanceSupportedIntervalsStrategy binanceSupportedIntervalsStrategy;

    @Spy
    private CoinbaseSupportedIntervalsStrategy coinbaseSupportedIntervalsStrategy;

    @Spy
    private KrakenSupportedIntervalsStrategy krakenSupportedIntervalsStrategy;

    @Spy
    private Map<Exchange, SupportedExchangeIntervalsStrategy> strategies = new HashMap<>();

    @InjectMocks
    private ChartWidgetServiceImpl service;

    @BeforeEach
    void setUp() {
        strategies.put(Exchange.BINANCE, binanceSupportedIntervalsStrategy);
        strategies.put(Exchange.COINBASE, coinbaseSupportedIntervalsStrategy);
        strategies.put(Exchange.KRAKEN, krakenSupportedIntervalsStrategy);
    }

    @Test
    void givenChartWidgetRequest_create_shouldCreateChartWidget() {
        when(repository.findMaxPositionByWorkspaceId(WORKSPACE_ID)).thenReturn(POSITION_0);
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(workspaceRepository.existsById(WORKSPACE_ID)).thenReturn(true);
        when(exchangePairRepository.existsById(EXCHANGE_PAIR_ID)).thenReturn(true);
        when(generator.uuid()).thenReturn(CHART_WIDGET_ID_1);
        when(generator.now()).thenReturn(CREATED_AT, CREATED_AT);
        when(repository.create(buildChartWidget()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChartWidget result = service.create(WORKSPACE_ID, buildChartWidgetRequest());

        assertThat(result).isEqualTo(buildChartWidget());
    }

    @Test
    void givenChartWidgetRequestWithNonExistentWorkspace_create_shouldThrowException() {
        when(workspaceRepository.existsById(WORKSPACE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.create(WORKSPACE_ID, buildChartWidgetRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).findMaxPositionByWorkspaceId(WORKSPACE_ID);
        verify(repository, never()).create(any(ChartWidget.class));
    }

    @Test
    void givenChartWidgetRequestWithNonExistentExchangePairId_create_shouldThrowException() {
        when(workspaceRepository.existsById(WORKSPACE_ID)).thenReturn(true);
        when(exchangePairRepository.existsById(EXCHANGE_PAIR_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.create(WORKSPACE_ID, buildChartWidgetRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).findMaxPositionByWorkspaceId(WORKSPACE_ID);
        verify(repository, never()).create(any(ChartWidget.class));
    }

    @ParameterizedTest
    @MethodSource("provideSupportedChartIntervals")
    void givenIdAndUpdateRequestWithSupportedChartIntervals_update_shouldUpdateChartWidget(Exchange exchange, ChartInterval chartInterval) {
        ChartInterval updatedChartInterval = chartInterval == ChartInterval.FIFTEEN_MINUTES
                ? ChartInterval.FIVE_MINUTES
                : chartInterval;
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .thenReturn(Optional.of(buildChartWidget(ChartInterval.FIFTEEN_MINUTES)));
        when(exchangePairRepository.findById(EXCHANGE_PAIR_ID)).thenReturn(Optional.of(buildExchangePair(exchange)));
        when(generator.now()).thenReturn(UPDATED_AT);
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.update(buildUpdatedChartWidget(updatedChartInterval)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChartWidget result = service.update(WORKSPACE_ID, CHART_WIDGET_ID_1,
                buildUpdateChartWidgetRequest(updatedChartInterval));

        assertThat(result).isEqualTo(buildUpdatedChartWidget(updatedChartInterval));
    }

    static Stream<Arguments> provideSupportedChartIntervals() {
        return Stream.of(
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_SECOND),
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_MINUTE),
                Arguments.of(Exchange.BINANCE, ChartInterval.THREE_MINUTES),
                Arguments.of(Exchange.BINANCE, ChartInterval.FIVE_MINUTES),
                Arguments.of(Exchange.BINANCE, ChartInterval.FIFTEEN_MINUTES),
                Arguments.of(Exchange.BINANCE, ChartInterval.THIRTY_MINUTES),
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_HOUR),
                Arguments.of(Exchange.BINANCE, ChartInterval.TWO_HOURS),
                Arguments.of(Exchange.BINANCE, ChartInterval.FOUR_HOURS),
                Arguments.of(Exchange.BINANCE, ChartInterval.SIX_HOURS),
                Arguments.of(Exchange.BINANCE, ChartInterval.EIGHT_HOURS),
                Arguments.of(Exchange.BINANCE, ChartInterval.TWELVE_HOURS),
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_DAY),
                Arguments.of(Exchange.BINANCE, ChartInterval.THREE_DAYS),
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_WEEK),
                Arguments.of(Exchange.BINANCE, ChartInterval.ONE_MONTH),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_MINUTE),
                Arguments.of(Exchange.COINBASE, ChartInterval.FIVE_MINUTES),
                Arguments.of(Exchange.COINBASE, ChartInterval.FIFTEEN_MINUTES),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_HOUR),
                Arguments.of(Exchange.COINBASE, ChartInterval.SIX_HOURS),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_DAY),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_MINUTE),
                Arguments.of(Exchange.KRAKEN, ChartInterval.FIVE_MINUTES),
                Arguments.of(Exchange.KRAKEN, ChartInterval.FIFTEEN_MINUTES),
                Arguments.of(Exchange.KRAKEN, ChartInterval.THIRTY_MINUTES),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_HOUR),
                Arguments.of(Exchange.KRAKEN, ChartInterval.FOUR_HOURS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_DAY),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_WEEK),
                Arguments.of(Exchange.KRAKEN, ChartInterval.FIFTEEN_DAYS)
        );
    }

    @ParameterizedTest
    @MethodSource("provideUnsupportedChartIntervals")
    void givenIdAndUpdateRequestWithUnsupportedChartIntervals_update_shouldUpdateChartWidget(Exchange exchange, ChartInterval chartInterval) {
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1)).thenReturn(Optional.of(buildChartWidget()));
        when(exchangePairRepository.findById(EXCHANGE_PAIR_ID)).thenReturn(Optional.of(buildExchangePair(exchange)));

        assertThatThrownBy(() -> service.update(WORKSPACE_ID, CHART_WIDGET_ID_1, buildUpdateChartWidgetRequest(chartInterval)))
                .isInstanceOf(UnsupportedIntervalException.class);

        verify(generator, never()).now();
        verify(repository, never()).update(any(ChartWidget.class));
    }

    static Stream<Arguments> provideUnsupportedChartIntervals() {
        return Stream.of(
                Arguments.of(Exchange.BINANCE, ChartInterval.FIFTEEN_DAYS),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_SECOND),
                Arguments.of(Exchange.COINBASE, ChartInterval.THREE_MINUTES),
                Arguments.of(Exchange.COINBASE, ChartInterval.THIRTY_MINUTES),
                Arguments.of(Exchange.COINBASE, ChartInterval.TWO_HOURS),
                Arguments.of(Exchange.COINBASE, ChartInterval.FOUR_HOURS),
                Arguments.of(Exchange.COINBASE, ChartInterval.EIGHT_HOURS),
                Arguments.of(Exchange.COINBASE, ChartInterval.TWELVE_HOURS),
                Arguments.of(Exchange.COINBASE, ChartInterval.THREE_DAYS),
                Arguments.of(Exchange.COINBASE, ChartInterval.FIFTEEN_DAYS),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_WEEK),
                Arguments.of(Exchange.COINBASE, ChartInterval.ONE_MONTH),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_SECOND),
                Arguments.of(Exchange.KRAKEN, ChartInterval.THREE_MINUTES),
                Arguments.of(Exchange.KRAKEN, ChartInterval.TWO_HOURS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.SIX_HOURS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.EIGHT_HOURS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.TWELVE_HOURS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.THREE_DAYS),
                Arguments.of(Exchange.KRAKEN, ChartInterval.ONE_MONTH)
        );
    }

    @Test
    void givenIdAndUpdateRequestWithSameChartInterval_update_shouldNotUpdateChartWidget() {
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .thenReturn(Optional.of(buildChartWidget()));

        ChartWidget result = service.update(WORKSPACE_ID, CHART_WIDGET_ID_1,
                buildUpdateChartWidgetRequest());

        assertThat(result).isEqualTo(buildChartWidget());
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
    void givenIdAndUpdateRequestWithNonExistentExchangePair_update_shouldThrowException() {
        when(repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1))
                .thenReturn(Optional.of(buildChartWidget(ChartInterval.ONE_HOUR)));
        when(exchangePairRepository.findById(EXCHANGE_PAIR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(WORKSPACE_ID, CHART_WIDGET_ID_1, buildUpdateChartWidgetRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(generator, never()).now();
        verify(repository, never()).update(any(ChartWidget.class));
    }

    @Test
    void givenUpdatePositionsRequest_updatePositions_shouldUpdateChartWidgetPositions() {
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
    void givenUpdatePositionsRequestForNonExistentWidget_updatePositions_shouldUpdateOnlyExisting() {
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
    void givenId_delete_shouldDeleteChartWidget() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1)).thenReturn(true);

        service.delete(WORKSPACE_ID, CHART_WIDGET_ID_1);

        verify(repository).deleteById(WORKSPACE_ID, CHART_WIDGET_ID_1);
    }

    @Test
    void givenNonExistentId_delete_shouldThrowException() {
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

        static ChartWidget buildUpdatedChartWidget() {
            return buildUpdatedChartWidget(ChartInterval.FIFTEEN_MINUTES);
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

        static UpdateChartWidgetRequest buildUpdateChartWidgetRequest() {
            return buildUpdateChartWidgetRequest(ChartInterval.FIFTEEN_MINUTES);
        }

        static UpdateChartWidgetRequest buildUpdateChartWidgetRequest(ChartInterval chartInterval) {
            return UpdateChartWidgetRequest.builder()
                    .chartInterval(chartInterval)
                    .build();
        }

        static ExchangePair buildExchangePair() {
            return buildExchangePair(Exchange.BINANCE);
        }

        static ExchangePair buildExchangePair(Exchange exchange) {
            return ExchangePair.builder()
                    .id(EXCHANGE_PAIR_ID)
                    .tradingPair(TradingPair.BTC_USD)
                    .exchange(exchange)
                    .build();
        }
    }
}