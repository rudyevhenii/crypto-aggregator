package dev.rudyevhenii.crypto_aggregator.auth.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.auth.controller.AuthControllerTest.TestResources.*;
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
class AuthControllerTest extends AbstractIntegrationTest {

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
    @DataSet(cleanBefore = true)
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/then/registered_user.yaml")
    void givenValidRegisterRequest_register_shouldRegisterNewUser() {
        when(generator.now()).thenReturn(NOW);
        when(generator.uuid()).thenReturn(ID);

        String actualResponse = given()
                .contentType(ContentType.JSON)
                .body(buildRegisterRequestJson())
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                buildTokenResponseJson(),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRegisterRequest")
    void givenInvalidRegisterRequest_register_shouldReturnStatusBadRequest(String body) {
        when(generator.now()).thenReturn(NOW);
        when(generator.uuid()).thenReturn(ID);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> provideInvalidRegisterRequest() {
        return Stream.of(
                Arguments.of(buildRegisterRequestWithInvalidEmailJson(null)),
                Arguments.of(buildRegisterRequestWithInvalidEmailJson("")),
                Arguments.of(buildRegisterRequestWithInvalidEmailJson("not-an-email")),
                Arguments.of(buildRegisterRequestWithInvalidPasswordJson(null)),
                Arguments.of(buildRegisterRequestWithInvalidPasswordJson("12345")),
                Arguments.of(buildRegisterRequestWithInvalidPasswordJson("a".repeat(101))),
                Arguments.of(buildRegisterRequestWithInvalidFirstNameJson(null)),
                Arguments.of(buildRegisterRequestWithInvalidFirstNameJson("J")),
                Arguments.of(buildRegisterRequestWithInvalidFirstNameJson("a".repeat(51))),
                Arguments.of(buildRegisterRequestWithInvalidLastNameJson(null)),
                Arguments.of(buildRegisterRequestWithInvalidLastNameJson("D")),
                Arguments.of(buildRegisterRequestWithInvalidLastNameJson("a".repeat(51)))
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/user.yaml")
    void givenExistingUserEmail_register_shouldReturnStatusConflict() {
        given()
                .contentType(ContentType.JSON)
                .body(buildRegisterRequestJson())
                .when()
                .post(REGISTER_URL)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @SneakyThrows
    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/user.yaml")
    void givenLoginRequest_login_shouldLoginUserAndReturnTokens() {
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .contentType(ContentType.JSON)
                .body(buildLoginRequestJson())
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                buildTokenResponseJson(),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLoginRequest")
    void givenInvalidLoginRequest_login_shouldReturnStatusBadRequest(String body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> provideInvalidLoginRequest() {
        return Stream.of(
                Arguments.of(buildLoginRequestWithInvalidEmailJson(null)),
                Arguments.of(buildLoginRequestWithInvalidEmailJson("")),
                Arguments.of(buildLoginRequestWithInvalidEmailJson("not-an-email")),
                Arguments.of(buildLoginRequestWithInvalidPasswordJson(null)),
                Arguments.of(buildLoginRequestWithInvalidPasswordJson("12345")),
                Arguments.of(buildLoginRequestWithInvalidPasswordJson("a".repeat(101)))
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/empty_users.yaml")
    void givenNonExistentUserEmail_login_shouldReturnStatusNonFound() {
        given()
                .contentType(ContentType.JSON)
                .body(buildLoginRequestJson())
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SneakyThrows
    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/user.yaml")
    void givenValidRefreshTokenRequest_refreshToken_shouldReturnRenewedTokens() {
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .contentType(ContentType.JSON)
                .body(buildRefreshTokenRequestJson())
                .when()
                .post(REFRESH_TOKEN_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                buildTokenResponseJson(),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRefreshTokenRequest")
    void givenInvalidRefreshTokenRequest_refreshToken_shouldReturnStatusBadRequest(String body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> provideInvalidRefreshTokenRequest() {
        return Stream.of(
                Arguments.of(buildRefreshTokenRequestWithInvalidRefreshTokenJson(null)),
                Arguments.of(buildRefreshTokenRequestWithInvalidRefreshTokenJson("invalid-refresh-token"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRefreshTokens")
    void givenAccessTokenForRefreshTokenRequest_refreshToken_shouldReturnStatusUnauthorized(String body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(REFRESH_TOKEN_URL)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    static Stream<Arguments> provideInvalidRefreshTokens() {
        return Stream.of(
                Arguments.of(buildRefreshTokenRequestWithInvalidRefreshTokenJson(ACCESS_TOKEN)),
                Arguments.of(buildRefreshTokenRequestWithInvalidRefreshTokenJson(EXPIRED_REFRESH_TOKEN)),
                Arguments.of(buildRefreshTokenRequestWithInvalidRefreshTokenJson(CORRUPTED_REFRESH_TOKEN))
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/empty_users.yaml")
    void givenNonExistentUserId_refreshToken_shouldReturnStatusNonFound() {
        given()
                .contentType(ContentType.JSON)
                .body(buildRefreshTokenRequestJson())
                .when()
                .post(REFRESH_TOKEN_URL)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/user.yaml")
    void givenLogoutRequest_logout_shouldInvalidateTokens() {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(JwtTokenUtils.buildAuthHeader(ID))
                .contentType(ContentType.JSON)
                .body(buildLogoutRequestJson())
                .when()
                .post(LOGOUT_URL)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLogoutRequest")
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/controller/datasets/given/user.yaml")
    void givenInvalidLogoutRequest_logout_shouldReturnStatusBadRequest(String body) {
        when(generator.now()).thenReturn(NOW);

        given()
                .header(JwtTokenUtils.buildAuthHeader(ID))
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(LOGOUT_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<Arguments> provideInvalidLogoutRequest() {
        return Stream.of(
                Arguments.of(buildLogoutRequestWithInvalidAccessTokenJson(null)),
                Arguments.of(buildLogoutRequestWithInvalidRefreshTokenJson(null))
        );
    }

    static class TestResources {
        static final String REGISTER_URL = "/api/auth/register";
        static final String LOGIN_URL = "/api/auth/login";
        static final String REFRESH_TOKEN_URL = "/api/auth/refresh-token";
        static final String LOGOUT_URL = "/api/auth/logout";

        static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String EMAIL = "JohnDoe@gmail.com";
        static final String PASSWORD = "password12345";
        static final String FIRST_NAME = "John";
        static final String LAST_NAME = "Doe";

        static final Instant NOW = Instant.now();

        static final Instant ACCESS_TOKEN_EXP = NOW.plus(60, ChronoUnit.MINUTES);
        static final Instant REFRESH_TOKEN_EXP = NOW.plus(7, ChronoUnit.DAYS);

        static final String ACCESS_TOKEN = JwtTokenUtils.buildAccessToken(ID, NOW, ACCESS_TOKEN_EXP);
        static final String REFRESH_TOKEN = JwtTokenUtils.buildRefreshToken(ID, NOW, REFRESH_TOKEN_EXP);

        static final String EXPIRED_REFRESH_TOKEN = JwtTokenUtils.buildExpiredRefreshToken(ID);
        static final String CORRUPTED_REFRESH_TOKEN = JwtTokenUtils.buildCorruptedRefreshToken(ID);

        static String buildRegisterRequestJson() {
            return buildRegisterRequestJson(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
        }

        static String buildRegisterRequestWithInvalidEmailJson(String email) {
            return buildRegisterRequestJson(email, PASSWORD, FIRST_NAME, LAST_NAME);
        }

        static String buildRegisterRequestWithInvalidPasswordJson(String password) {
            return buildRegisterRequestJson(EMAIL, password, FIRST_NAME, LAST_NAME);
        }

        static String buildRegisterRequestWithInvalidFirstNameJson(String firstName) {
            return buildRegisterRequestJson(EMAIL, PASSWORD, firstName, LAST_NAME);
        }

        static String buildRegisterRequestWithInvalidLastNameJson(String lastName) {
            return buildRegisterRequestJson(EMAIL, PASSWORD, FIRST_NAME, lastName);
        }

        static String buildRegisterRequestJson(String email, String password, String firstName, String lastName) {
            return """
                    {
                      "email": %s,
                      "password": %s,
                      "firstName": %s,
                      "lastName": %s
                    }""".formatted(toJsonValue(email), toJsonValue(password), toJsonValue(firstName), toJsonValue(lastName));
        }

        static String buildLoginRequestJson() {
            return buildLoginRequestJson(EMAIL, PASSWORD);
        }

        static String buildLoginRequestWithInvalidEmailJson(String email) {
            return buildLoginRequestJson(email, PASSWORD);
        }

        static String buildLoginRequestWithInvalidPasswordJson(String password) {
            return buildLoginRequestJson(EMAIL, password);
        }

        static String buildLoginRequestJson(String email, String password) {
            return """
                    {
                      "email": %s,
                      "password": %s
                    }""".formatted(toJsonValue(email), toJsonValue(password));
        }

        static String buildRefreshTokenRequestJson() {
            return buildRefreshTokenRequestJson(REFRESH_TOKEN);
        }

        static String buildRefreshTokenRequestWithInvalidRefreshTokenJson(String refreshToken) {
            return buildRefreshTokenRequestJson(refreshToken);
        }

        static String buildRefreshTokenRequestJson(String refreshToken) {
            return """
                    {
                      "refreshToken": %s
                    }""".formatted(toJsonValue(refreshToken));
        }

        static String buildLogoutRequestJson() {
            return buildLogoutRequestJson(ACCESS_TOKEN, REFRESH_TOKEN);
        }

        static String buildLogoutRequestWithInvalidAccessTokenJson(String accessToken) {
            return buildLogoutRequestJson(accessToken, REFRESH_TOKEN);
        }

        static String buildLogoutRequestWithInvalidRefreshTokenJson(String refreshToken) {
            return buildLogoutRequestJson(ACCESS_TOKEN, refreshToken);
        }

        static String buildLogoutRequestJson(String accessToken, String refreshToken) {
            return """
                    {
                      "accessToken": %s,
                      "refreshToken": %s
                    }""".formatted(toJsonValue(accessToken), toJsonValue(refreshToken));
        }

        static String buildTokenResponseJson() {
            return """
                    {
                      "accessToken": "%s",
                      "refreshToken": "%s"
                    }""".formatted(ACCESS_TOKEN, REFRESH_TOKEN);
        }

        private static String toJsonValue(String value) {
            return value == null ? "null" : "\"%s\"".formatted(value);
        }
    }
}