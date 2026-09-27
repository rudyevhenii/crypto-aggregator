package dev.rudyevhenii.crypto_aggregator.price_alert.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.CustomPostgresDataTypeFactory;
import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import dev.rudyevhenii.crypto_aggregator.utils.TestUtils;
import io.restassured.RestAssured;
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

import static dev.rudyevhenii.crypto_aggregator.price_alert.controller.PriceAlertLogControllerTest.TestResources.*;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@DBUnit(
        dataTypeFactoryClass = CustomPostgresDataTypeFactory.class,
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class PriceAlertLogControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private GeneratorUtils generator;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert_for_logs.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert_log.yaml"
    })
    void givenNothing_getAllAlertLogs_shouldReturnPriceAlertLogs() {
        when(generator.now()).thenReturn(NOW);
        when(userContext.getUserId()).thenReturn(USER_ID);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_PRICE_ALERT_LOG_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                TestUtils.readResource("dev/rudyevhenii/crypto_aggregator/price_alert/controller/json/getAll_response_priceAlertLog.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("invalidScrollRequestLimitValues")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert_for_logs.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert_log.yaml"
    })
    void givenInvalidPriceAlertLogScrollRequest_getAllAlertLogs_shouldReturnStatusBadRequest(int limit) {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .param(PriceAlertLogScrollRequest.Fields.limit, limit)
                .when()
                .get(BASE_PRICE_ALERT_LOG_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> invalidScrollRequestLimitValues() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(101)
        );
    }

    static class TestResources {
        static final String BASE_PRICE_ALERT_LOG_URL = "/api/price-alert-logs";

        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final Instant NOW = Instant.now();
        static final Header AUTH_HEADER = JwtTokenUtils.buildAuthHeader(USER_ID);
    }
}