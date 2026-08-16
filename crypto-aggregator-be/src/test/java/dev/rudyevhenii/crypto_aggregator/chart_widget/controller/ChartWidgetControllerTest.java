package dev.rudyevhenii.crypto_aggregator.chart_widget.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.chart_widget.controller.ChartWidgetControllerTest.TestResources.*;
import static dev.rudyevhenii.crypto_aggregator.utils.TestUtils.readResource;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class ChartWidgetControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private GeneratorUtils generator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/then/created_chartWidget.yaml")
    void givenChartWidgetRequest_createChartWidget_shouldCreateNewChartWidget() {
        when(generator.uuid()).thenReturn(ID_2);
        when(generator.now()).thenReturn(NOW, CREATED_AT, CREATED_AT);

        String actualResponse = given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildChartWidgetRequestJson())
                .when()
                .post(BASE_CHART_WIDGET_URL, WORKSPACE_ID_1)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/json/createChartWidget_response_createdChartWidget.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParamsForCreate")
    @DataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml")
    void givenInvalidChartWidgetRequest_createChartWidget_shouldReturnStatusBadRequest(String workspaceId, String body) {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(body)
                .when()
                .post(BASE_CHART_WIDGET_URL, workspaceId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> provideInvalidParamsForCreate() {
        return Stream.of(
                Arguments.of(NON_VALID_UUID, buildChartWidgetRequestJson()),
                Arguments.of(WORKSPACE_ID_1.toString(), buildInvalidChartWidgetRequestJson())
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    void givenChartWidgetRequestWithNonExistentWorkspaceId_createChartWidget_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildChartWidgetRequestJson())
                .when()
                .post(BASE_CHART_WIDGET_URL, NON_EXISTENT_WORKSPACE_ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    void givenChartWidgetRequestWithNonExistentExchangePairId_createChartWidget_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildNonExistentChartWidgetRequestJson())
                .when()
                .post(BASE_CHART_WIDGET_URL, WORKSPACE_ID_1)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/then/updated_chartWidget.yaml")
    void givenIdAndUpdateChartWidgetRequest_updateChartWidget_shouldUpdateChartWidget() {
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        String actualResponse = given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildUpdateChartWidgetRequestJson())
                .when()
                .patch(BASE_CHART_WIDGET_URL + "/{chartWidgetId}", WORKSPACE_ID_1, ID_1)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/json/updateChartWidget_response_updatedChartWidget.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml")
    void givenInvalidParams_updateChartWidget_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildUpdateChartWidgetRequestJson())
                .when()
                .patch(BASE_CHART_WIDGET_URL + "/{chartWidgetId}", NON_VALID_UUID, ID_1)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/empty_chartWidget.yaml",
    })
    void givenEmptyChartWidgets_updateChartWidget_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildUpdateChartWidgetRequestJson())
                .when()
                .patch(BASE_CHART_WIDGET_URL + "/{chartWidgetId}", WORKSPACE_ID_1, ID_1)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/then/updatedPositions_chartWidget.yaml")
    void givenUpdateChartWidgetPositionsRequest_updateChartWidgetPositions_shouldUpdateChartWidgetsPositions() {
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildUpdateChartWidgetPositionsRequestJson())
                .when()
                .put(BASE_CHART_WIDGET_URL + "/positions", WORKSPACE_ID_2)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml")
    void givenInvalidUpdateChartWidgetPositionsRequest_updateChartWidgetPositions_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER)
                .body(buildInvalidUpdateChartWidgetPositionsRequestJson())
                .when()
                .put(BASE_CHART_WIDGET_URL + "/positions", WORKSPACE_ID_2)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    void givenWorkspaceId_getAllByWorkspaceId_shouldReturnAllChartWidgets() {
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_CHART_WIDGET_URL, WORKSPACE_ID_2)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/json/getAllByWorkspaceId_response_allChartWidgetsForWorkspace.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml")
    void givenInvalidWorkspaceId_getAllByWorkspaceId_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_CHART_WIDGET_URL, NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/workspace.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/exchangePair.yaml",
            "dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/chartWidget.yaml",
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/then/deleted_chartWidget.yaml")
    void givenId_deleteChartWidget_shouldDeleteChartWidget() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_CHART_WIDGET_URL + "/{chartWidgetId}", WORKSPACE_ID_1, ID_1)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/chart_widget/controller/datasets/given/user.yaml")
    void givenInvalidParams_deleteChartWidget_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_CHART_WIDGET_URL + "/{chartWidgetId}", NON_VALID_UUID, ID_1)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static class TestResources {
        static final String BASE_CHART_WIDGET_URL = "/api/workspaces/{workspaceId}/widgets";

        static final UUID ID_1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        static final UUID ID_2 = UUID.fromString("13333333-3333-3333-3333-333333333331");

        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
        static final UUID WORKSPACE_ID_1 = UUID.fromString("20000000-0000-0000-0000-000000000002");
        static final UUID WORKSPACE_ID_2 = UUID.fromString("21111111-1111-1111-1111-111111111112");
        static final UUID EXCHANGE_PAIR_ID_1 = UUID.fromString("31111111-1111-1111-1111-111111111113");

        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final UUID NON_EXISTENT_EXCHANGE_PAIR_ID = UUID.fromString("a898989f-aaaa-aaaa-aaaa-a8989898989a");
        static final UUID NON_EXISTENT_WORKSPACE_ID = UUID.fromString("b898989f-bbbb-bbbb-bbbb-b8989898989b");

        static final String NON_VALID_UUID = "not-a-valid-uuid";

        static final Instant NOW = Instant.now();

        static final Header AUTH_HEADER = JwtTokenUtils.buildAuthHeader(USER_ID);

        static String buildChartWidgetRequestJson() {
            return """
                    {
                      "exchangePairId": "%s"
                    }
                    """.formatted(EXCHANGE_PAIR_ID_1);
        }

        static String buildInvalidChartWidgetRequestJson() {
            return """
                    {
                      "exchangePairId": "invalid-uuid"
                    }
                    """;
        }

        static String buildNonExistentChartWidgetRequestJson() {
            return """
                    {
                      "exchangePairId": "%s"
                    }
                    """.formatted(NON_EXISTENT_EXCHANGE_PAIR_ID);
        }

        static String buildUpdateChartWidgetRequestJson() {
            return """
                    {
                      "chartInterval": "THIRTY_MINUTES"
                    }
                    """;
        }

        static String buildUpdateChartWidgetPositionsRequestJson() {
            return """
                    [
                      {
                        "chartWidgetId": "11111111-1111-1111-1111-111111111111",
                        "position": 2
                      },
                      {
                        "chartWidgetId": "12222222-2222-2222-2222-222222222221",
                        "position": 1
                      }
                    ]
                    """;
        }

        static String buildInvalidUpdateChartWidgetPositionsRequestJson() {
            return """
                    [
                      {
                        "chartWidgetId": "11111111-1111-1111-1111-111111111111",
                        "position": -90
                      },
                      {
                        "chartWidgetId": "12222222-2222-2222-2222-222222222221",
                        "position": 26
                      }
                    ]
                    """;
        }
    }
}