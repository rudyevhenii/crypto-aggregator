package dev.rudyevhenii.crypto_aggregator.chart_widget.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.chart_widget.repository.DefaultChartWidgetRepositoryTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class DefaultChartWidgetRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ChartWidgetRepository repository;

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/then/created_chartWidget.yaml")
    void givenChartWidget_create_shouldCreateNewChartWidget() {
        ChartWidget result = repository.create(buildChartWidget());
        assertThat(result).isEqualTo(buildChartWidget());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/then/updated_chartWidget.yaml")
    void givenChartWidget_update_shouldUpdateExistingChartWidget() {
        ChartWidget result = repository.update(buildUpdatedChartWidget());
        assertThat(result).isEqualTo(buildUpdatedChartWidget());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/then/updatedPositions_chartWidget.yaml")
    void givenWorkspaceIdAndChartWidgets_updatePositions_shouldUpdateChartWidgetPositions() {
        repository.updatePositions(WORKSPACE_ID_1, buildUpdatedChartWidgetList());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenWorkspaceIdAndId_findByWorkspaceIdAndId_shouldReturnChartWidget() {
        Optional<ChartWidget> result = repository.findByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1);
        assertThat(result).contains(buildFoundChartWidget());
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentIds")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenNonExistentWorkspaceIdAndId_findByWorkspaceIdAndId_shouldReturnEmptyOptional(UUID workspaceId, UUID id) {
        Optional<ChartWidget> result = repository.findByWorkspaceIdAndId(workspaceId, id);
        assertThat(result).isEmpty();
    }

    static Stream<Arguments> provideNonExistentIds() {
        return Stream.of(
                Arguments.of(NON_EXISTENT_WORKSPACE_ID, NON_EXISTENT_ID),
                Arguments.of(NON_EXISTENT_WORKSPACE_ID, CHART_WIDGET_ID),
                Arguments.of(WORKSPACE_ID, NON_EXISTENT_ID)
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenWorkspaceId_findAllByWorkspaceId_shouldReturnAllChartWidgetsFromWorkspace() {
        List<ChartWidget> result = repository.findAllByWorkspaceId(WORKSPACE_ID_1);
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(buildChartWidgetList());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/empty_chartWidget.yaml"
    })
    void givenWorkspaceId_findAllByWorkspaceId_shouldReturnEmptyList() {
        List<ChartWidget> result = repository.findAllByWorkspaceId(WORKSPACE_ID);
        assertThat(result).isEmpty();
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/then/deleted_chartWidget.yaml")
    void givenWorkspaceIdAndId_deleteById_shouldDeleteChartWidget() {
        repository.deleteById(WORKSPACE_ID, CHART_WIDGET_ID_1);
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenWorkspaceId_findMaxPositionByWorkspaceId_shouldReturnCalculatedMaxPosition() {
        int result = repository.findMaxPositionByWorkspaceId(WORKSPACE_ID_1);
        assertThat(result).isEqualTo(POSITION_2);
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/empty_chartWidget.yaml"
    })
    void givenEmptyChartWidgets_findMaxPositionByWorkspaceId_shouldReturnZero() {
        int result = repository.findMaxPositionByWorkspaceId(WORKSPACE_ID_1);
        assertThat(result).isEqualTo(POSITION_0);
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenWorkspaceIdAndId_existsByWorkspaceIdAndId_shouldReturnTrue() {
        boolean result = repository.existsByWorkspaceIdAndId(WORKSPACE_ID, CHART_WIDGET_ID_1);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentIds")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/repository/datasets/given/chartWidget.yaml"
    })
    void givenNonExistentWorkspaceIdAndId_existsByWorkspaceIdAndId_shouldReturnFalse(UUID workspaceId, UUID id) {
        boolean result = repository.existsByWorkspaceIdAndId(workspaceId, id);
        assertThat(result).isFalse();
    }

    static class TestResources {
        static final UUID CHART_WIDGET_ID = UUID.fromString("13333333-3333-3333-3333-333333333331");
        static final UUID EXCHANGE_PAIR_ID = UUID.fromString("31111111-1111-1111-1111-111111111113");
        static final UUID WORKSPACE_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");

        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final int POSITION_0 = 0;
        static final int POSITION_1 = 1;
        static final int POSITION_2 = 2;

        static final UUID CHART_WIDGET_ID_1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        static final UUID CHART_WIDGET_ID_2 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final UUID CHART_WIDGET_ID_3 = UUID.fromString("12222222-2222-2222-2222-222222222221");

        static final UUID EXCHANGE_PAIR_ID_1 = UUID.fromString("30000000-0000-0000-0000-000000000003");
        static final UUID EXCHANGE_PAIR_ID_2 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        static final UUID EXCHANGE_PAIR_ID_3 = UUID.fromString("37777777-7777-7777-7777-777777777773");

        static final UUID WORKSPACE_ID_1 = UUID.fromString("21111111-1111-1111-1111-111111111112");

        static final UUID NON_EXISTENT_ID = UUID.fromString("f898989f-ffff-ffff-ffff-f8989898989f");
        static final UUID NON_EXISTENT_WORKSPACE_ID = UUID.fromString("a898989f-aaaa-aaaa-aaaa-a8989898989a");

        static ChartWidget buildChartWidget() {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID)
                    .chartInterval(ChartInterval.ONE_HOUR)
                    .exchangePairId(EXCHANGE_PAIR_ID)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_2)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static ChartWidget buildUpdatedChartWidget() {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID_1)
                    .chartInterval(ChartInterval.THIRTY_MINUTES)
                    .exchangePairId(EXCHANGE_PAIR_ID_1)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_1)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static List<ChartWidget> buildUpdatedChartWidgetList() {
            return List.of(
                    buildUpdatedPositionsChartWidget(CHART_WIDGET_ID_2, ChartInterval.ONE_HOUR, EXCHANGE_PAIR_ID_2, POSITION_2),
                    buildUpdatedPositionsChartWidget(CHART_WIDGET_ID_3, ChartInterval.FIVE_MINUTES, EXCHANGE_PAIR_ID_3, POSITION_1)
            );
        }

        static ChartWidget buildUpdatedPositionsChartWidget(UUID id, ChartInterval chartInterval, UUID exchangePairId, int position) {
            return ChartWidget.builder()
                    .id(id)
                    .chartInterval(chartInterval)
                    .exchangePairId(exchangePairId)
                    .workspaceId(WORKSPACE_ID_1)
                    .position(position)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();
        }

        static ChartWidget buildFoundChartWidget() {
            return ChartWidget.builder()
                    .id(CHART_WIDGET_ID_1)
                    .chartInterval(ChartInterval.FIFTEEN_MINUTES)
                    .exchangePairId(EXCHANGE_PAIR_ID_1)
                    .workspaceId(WORKSPACE_ID)
                    .position(POSITION_1)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }

        static List<ChartWidget> buildChartWidgetList() {
            return List.of(
                    buildChartWidget(CHART_WIDGET_ID_2, ChartInterval.ONE_HOUR, EXCHANGE_PAIR_ID_2, POSITION_1),
                    buildChartWidget(CHART_WIDGET_ID_3, ChartInterval.FIVE_MINUTES, EXCHANGE_PAIR_ID_3, POSITION_2)
            );
        }

        static ChartWidget buildChartWidget(UUID id, ChartInterval chartInterval, UUID exchangePairId, int position) {
            return ChartWidget.builder()
                    .id(id)
                    .chartInterval(chartInterval)
                    .exchangePairId(exchangePairId)
                    .workspaceId(WORKSPACE_ID_1)
                    .position(position)
                    .createdAt(CREATED_AT)
                    .updatedAt(CREATED_AT)
                    .build();
        }
    }
}