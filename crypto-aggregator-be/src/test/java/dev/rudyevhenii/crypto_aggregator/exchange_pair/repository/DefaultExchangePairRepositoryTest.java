package dev.rudyevhenii.crypto_aggregator.exchange_pair.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.DefaultExchangePairRepositoryTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class DefaultExchangePairRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ExchangePairRepository exchangePairRepository;

    @MockitoSpyBean
    private SpringDataExchangePairRepository repositorySpy;

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/given/exchange_pairs.yml")
    void givenNothing_findAllTradingPairs_shouldReturnAllExchangePairs() {
        assertThat(exchangePairRepository.findAllTradingPairs()).isNotEmpty();
        verify(repositorySpy).findAllByOrderByTradingPairAscExchange();

        assertThat(exchangePairRepository.findAllTradingPairs()).isNotEmpty();
        verifyNoMoreInteractions(repositorySpy);
    }

    @ParameterizedTest
    @MethodSource("provideExchanges")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/given/exchange_pairs.yml")
    void givenExchange_searchByPattern_shouldReturnExchangePairsForSpecifiedExchange(Exchange exchange) {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(exchange, EMPTY_TRADING_PAIR_VALUE);
        assertThat(exchangePairs).isNotEmpty();
        assertThat(exchangePairs)
                .extracting(ExchangePair::getExchange)
                .containsOnly(exchange);

        assertThat(exchangePairs).isSortedAccordingTo(NATURAL_SORTING_ORDER);
    }

    static Stream<Arguments> provideExchanges() {
        return Stream.of(
                Arguments.of(Exchange.BINANCE),
                Arguments.of(Exchange.COINBASE),
                Arguments.of(Exchange.KRAKEN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTradingPairPatterns")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/given/exchange_pairs.yml")
    void givenTradingPairPattern_searchByPattern_shouldReturnExchangePairsWithSpecifiedPatternForTradingPair(String tradingPair) {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(NULL_EXCHANGE, tradingPair);
        assertThat(exchangePairs).isNotEmpty();
        assertThat(exchangePairs).allMatch(exchangePair ->
                exchangePair.getTradingPair().name().contains(tradingPair.toUpperCase()));

        assertThat(exchangePairs).isSortedAccordingTo(NATURAL_SORTING_ORDER);
    }

    public static Stream<Arguments> provideTradingPairPatterns() {
        return Stream.of(
                Arguments.of("ad"),
                Arguments.of("Bt"),
                Arguments.of("N"),
                Arguments.of("OM"),
                Arguments.of("oG"),
                Arguments.of("GR"),
                Arguments.of("tH"),
                Arguments.of("k")
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/given/exchange_pairs.yml")
    void givenNonExistingTradingPairPattern_searchByPattern_shouldReturnExchangePairsForSpecifiedExchange() {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(NULL_EXCHANGE, NON_EXISTING_TRADING_PAIR_VALUE);
        assertThat(exchangePairs).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideExchangesAndTradingPairPattern")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/given/exchange_pairs.yml")
    void givenExchangeAndTradingPair_searchByPattern_shouldReturnExchangePairs(Exchange exchange, String tradingPair) {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(exchange, tradingPair);
        assertThat(exchangePairs).isNotEmpty();
        assertThat(exchangePairs).allMatch(exchangePair ->
                exchangePair.getTradingPair().name().contains(tradingPair.toUpperCase()));
        assertThat(exchangePairs)
                .extracting(ExchangePair::getExchange)
                .containsOnly(exchange);

        assertThat(exchangePairs).isSortedAccordingTo(NATURAL_SORTING_ORDER);
    }

    public static Stream<Arguments> provideExchangesAndTradingPairPattern() {
        return Stream.of(
                Arguments.of(Exchange.BINANCE, "d"),
                Arguments.of(Exchange.COINBASE, "K"),
                Arguments.of(Exchange.KRAKEN, "l")
        );
    }

    static class TestResources {
        public static final String EMPTY_TRADING_PAIR_VALUE = "";
        public static final String NON_EXISTING_TRADING_PAIR_VALUE = "JSDF";

        public static final Exchange NULL_EXCHANGE = null;

        public static final Comparator<ExchangePair> NATURAL_SORTING_ORDER =
                Comparator.comparing((ExchangePair exchangePair) -> exchangePair.getTradingPair().name())
                        .thenComparing(exchangePair -> exchangePair.getExchange().name());
    }
}