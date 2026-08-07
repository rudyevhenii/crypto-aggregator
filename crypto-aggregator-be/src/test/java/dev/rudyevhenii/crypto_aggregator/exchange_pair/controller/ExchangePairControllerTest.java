package dev.rudyevhenii.crypto_aggregator.exchange_pair.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import io.restassured.RestAssured;
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

import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.exchange_pair.controller.ExchangePairControllerTest.TestResources.*;
import static dev.rudyevhenii.crypto_aggregator.utils.TestUtils.readResource;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class ExchangePairControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/users.yaml",
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/exchange_pairs.yaml"
    })
    void givenValidAccessToken_findAllExchangePairs_shouldReturnAllExchangePairs() {
        String actualResponse = given()
                .header(JwtTokenUtils.buildAuthHeader(USER_ID))
                .when()
                .get(REQUEST_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/json/findAll_response_exchangePairs.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    void givenNoToken_findAllExchangePairs_shouldReturnStatusUnauthorized() {
        when()
                .get(REQUEST_URL)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/users.yaml",
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/exchange_pairs.yaml"
    })
    void givenNullExchangeAndTradingPairPattern_searchExchangePairs_shouldReturnExchangePairs() {
        String actualResponse = given()
                .header(JwtTokenUtils.buildAuthHeader(USER_ID))
                .when()
                .get(REQUEST_SEARCH_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/json/searchExchangePairs_response_nullExchangeAndTradingPair.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideExchangeAndTradingPairs")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/users.yaml",
            "dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/datasets/exchange_pairs.yaml"
    })
    void givenExchangeAndTradingPair_searchExchangePairs_shouldReturnExchangePairs(Exchange exchange, String tradingPair, String expectedResponse) {
        String actualResponse = given()
                .header(JwtTokenUtils.buildAuthHeader(USER_ID))
                .queryParam(EXCHANGE_PARAM, exchange)
                .queryParam(TRADING_PAIR_PARAM, tradingPair)
                .when()
                .get(REQUEST_SEARCH_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                expectedResponse,
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    public static Stream<Arguments> provideExchangeAndTradingPairs() {
        return Stream.of(
                Arguments.of(Exchange.BINANCE, null,
                        readResource("dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/json/searchExchangePairs_response_binanceExchangeAndNullTradingPair.json")),
                Arguments.of(null, "BT",
                        readResource("dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/json/searchExchangePairs_response_nullExchangeAnd_BT_tradingPair.json")),
                Arguments.of(Exchange.COINBASE, "ET",
                        readResource("dev/rudyevhenii/crypto_aggregator/exchange_pair/controller/json/searchExchangePairs_response_coinbaseExchangeAnd_ET_tradingPair.json"))
        );
    }

    static class TestResources {
        static final String REQUEST_URL = "/api/exchange-pairs";
        static final String REQUEST_SEARCH_URL = "/api/exchange-pairs/search";

        static final String EXCHANGE_PARAM = "exchange";
        static final String TRADING_PAIR_PARAM = "tradingPair";

        static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}