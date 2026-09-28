package dev.rudyevhenii.crypto_aggregator.price_alert.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.CustomPostgresDataTypeFactory;
import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.price_alert.controller.PriceAlertControllerTest.TestResources.*;
import static dev.rudyevhenii.crypto_aggregator.utils.TestUtils.readResource;
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
class PriceAlertControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private GeneratorUtils generator;

    @MockitoBean
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/then/created_price_alert.yaml")
    void givenPriceAlertRequest_createPriceAlert_shouldCreateNewPriceAlert() {
        when(generator.uuid()).thenReturn(ID);
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, CREATED_AT);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildPriceAlertRequestJson())
                .when()
                .post(BASE_PRICE_ALERT_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/price_alert/controller/json/create_response_priceAlert.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPriceAlertRequest")
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidPriceAlertRequest_createPriceAlert_shouldReturnStatusBadRequest(String request) {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PRICE_ALERT_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> invalidPriceAlertRequest() {
        return Stream.of(
                Arguments.of(buildInvalidRequestWithNullFieldsJson()),
                Arguments.of(TestResources.buildInvalidRequestWithEmptyDeliveryMethodsJson())
        );
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/then/updated_price_alert.yaml")
    void givenUpdateRequest_updatePriceAlert_shouldUpdatePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildPriceAlertUpdateRequestJson())
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/price_alert/controller/json/update_response_priceAlert.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPriceAlertUpdateRequest")
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidUpdateRequest_updatePriceAlert_shouldReturnStatusBadRequest(String request) {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> invalidPriceAlertUpdateRequest() {
        return Stream.of(
                Arguments.of(buildUpdateRequestWithNullFieldsJson()),
                Arguments.of(buildUpdateRequestWithEmptyDeliveryMethodsJson())
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/empty_price_alert.yaml"
    })
    void givenUpdateRequest_updatePriceAlert_shouldReturnStatusNotFound() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildPriceAlertUpdateRequestJson())
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    void givenId_getPriceAlertById_shouldReturnPriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/price_alert/controller/json/getById_response_priceAlert.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidId_getPriceAlertById_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_PRICE_ALERT_URL + "/{priceAlertId}", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/empty_price_alert.yaml"
    })
    void givenNonExistentId_getPriceAlertById_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    void givenNothing_getAllPriceAlerts_shouldReturnPriceAlerts() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_PRICE_ALERT_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/price_alert/controller/json/getAll_response_priceAlert.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/then/activated_price_alert.yaml")
    void givenId_activatePriceAlert_shouldActivatePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/activate", ID_2)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidId_activatePriceAlert_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/activate", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/empty_price_alert.yaml"
    })
    void givenNonExistentId_activatePriceAlert_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/activate", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/then/deactivated_price_alert.yaml")
    void givenId_deactivatePriceAlert_shouldDeactivatePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/deactivate", ID)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidId_deactivatePriceAlert_shouldReturnStatusBadRequest() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/deactivate", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/empty_price_alert.yaml"
    })
    void givenNonExistentId_deactivatePriceAlert_shouldReturnStatusNotFound() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .patch(BASE_PRICE_ALERT_URL + "/{priceAlertId}/deactivate", ID_2)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/price_alert.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/then/deleted_price_alert.yaml")
    void givenId_deletePriceAlert_shouldDeletePriceAlert() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_PRICE_ALERT_URL + "/{priceAlertId}", ID)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/price_alert/controller/datasets/given/user.yaml")
    void givenInvalidId_deletePriceAlert_shouldReturnStatusBadRequest() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_PRICE_ALERT_URL + "/{priceAlertId}", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static class TestResources {
        static final String BASE_PRICE_ALERT_URL = "/api/price-alerts";

        static final UUID ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
        static final UUID ID_2 = UUID.fromString("51111111-1111-1111-1111-111111111115");

        static final UUID USER_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");

        static final String NON_VALID_UUID = "not-a-valid-uuid";

        static final Instant NOW = Instant.now();
        static final Instant EXPIRES_AT = Instant.parse("2026-09-08T12:00:00Z");
        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final BigDecimal TARGET_PRICE = new BigDecimal("85000.00");
        static final int COOLDOWN_MINUTES = 5;
        static final BigDecimal INITIAL_PRICE = new BigDecimal("73500.00");
        static final BigDecimal PERCENTAGE_CHANGE = new BigDecimal("3.25");

        static final Header AUTH_HEADER = JwtTokenUtils.buildAuthHeader(USER_ID);

        static String buildPriceAlertRequestJson() {
            return """
                    {
                      "exchange": "BINANCE",
                      "tradingPair": "BTC_USD",
                      "triggerPolicy": {
                        "triggerType": "RECURRING",
                        "cooldownMinutes": %s
                      },
                      "deliveryMethods": [
                        "EMAIL"
                      ],
                      "conditionPayload": {
                        "targetPrice": %s,
                        "conditionType": "%s"
                      },
                      "expiresAt": "%s"
                    }""".formatted(COOLDOWN_MINUTES, TARGET_PRICE,
                    ConditionType.GREATER_THAN, EXPIRES_AT);
        }

        static String buildInvalidRequestWithNullFieldsJson() {
            return """
                    {
                      "exchange": null,
                      "tradingPair": null,
                      "triggerPolicy": null,
                      "deliveryMethods": null,
                      "conditionPayload": null,
                      "expiresAt": null
                    }""";
        }

        static String buildInvalidRequestWithEmptyDeliveryMethodsJson() {
            return """
                    {
                      "exchange": "BINANCE",
                      "tradingPair": "BTC_USD",
                      "triggerPolicy": {
                        "triggerType": "RECURRING",
                        "cooldownMinutes": %s
                      },
                      "deliveryMethods": [],
                      "conditionPayload": {
                        "targetPrice": %s,
                        "conditionType": "%s"
                      },
                      "expiresAt": null
                    }""".formatted(COOLDOWN_MINUTES, TARGET_PRICE, ConditionType.GREATER_THAN);
        }

        static String buildPriceAlertUpdateRequestJson() {
            return """
                    {
                      "triggerPolicy": {
                        "triggerType": "ONE_TIME"
                      },
                      "deliveryMethods": [
                        "EMAIL"
                      ],
                      "conditionPayload": {
                        "initialPrice": %s,
                        "conditionType": "%s",
                        "percentageChange": %s
                      },
                      "expiresAt": null
                    }""".formatted(INITIAL_PRICE, ConditionType.PERCENT_UP, PERCENTAGE_CHANGE);
        }

        static String buildUpdateRequestWithNullFieldsJson() {
            return """
                    {
                      "triggerPolicy": null,
                      "deliveryMethods": null,
                      "conditionPayload": null,
                      "expiresAt": null
                    }""";
        }

        static String buildUpdateRequestWithEmptyDeliveryMethodsJson() {
            return """
                    {
                      "triggerPolicy": {
                        "triggerType": "ONE_TIME"
                      },
                      "deliveryMethods": [],
                      "conditionPayload": {
                        "initialPrice": %s,
                        "conditionType": "%s",
                        "percentageChange": %s
                      },
                      "expiresAt": "%s"
                    }""".formatted(INITIAL_PRICE, ConditionType.PERCENT_UP,
                    PERCENTAGE_CHANGE, EXPIRES_AT);
        }
    }
}