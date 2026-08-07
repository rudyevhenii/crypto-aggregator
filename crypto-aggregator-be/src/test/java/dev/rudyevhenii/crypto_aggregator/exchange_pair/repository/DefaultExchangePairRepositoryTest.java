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
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/datasets/given/exchange_pairs.yaml")
    void givenNothing_findAllTradingPairs_shouldReturnAllExchangePairs() {
        assertThat(exchangePairRepository.findAllExchangePairs()).isNotEmpty();
        verify(repositorySpy).findAllByOrderByTradingPairAscExchange();
    }

    @ParameterizedTest
    @MethodSource("provideExchanges")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/datasets/given/exchange_pairs.yaml")
    void givenExchange_searchByPattern_shouldReturnExchangePairsForSpecifiedExchange(Exchange exchange) {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(exchange, EMPTY_TRADING_PAIR_VALUE);
        assertThat(exchangePairs).isNotEmpty();
        assertThat(exchangePairs)
                .extracting(ExchangePair::getExchange)
                .containsOnly(exchange);

        assertThat(exchangePairs).isSortedAccordingTo(NATURAL_SORTING_ORDER);
    }

    static Stream<Arguments> provideExchanges() {
        Stream.Builder<Arguments> argsBuilder = Stream.builder();
        for (Exchange exchange : Exchange.values()) {
            argsBuilder.add(Arguments.of(exchange));
        }
        return argsBuilder.build();
    }

    @ParameterizedTest
    @MethodSource("provideTradingPairPatterns")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/datasets/given/exchange_pairs.yaml")
    void givenTradingPairPattern_searchByPattern_shouldReturnExchangePairsWithSpecifiedPatternForTradingPair(String tradingPair) {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(EXCHANGE_NULL_VALUE, tradingPair);
        assertThat(exchangePairs).isNotEmpty();
        assertThat(exchangePairs).allMatch(exchangePair ->
                exchangePair.getTradingPair().name().contains(tradingPair.toUpperCase()));

        assertThat(exchangePairs).isSortedAccordingTo(NATURAL_SORTING_ORDER);
    }

    static Stream<Arguments> provideTradingPairPatterns() {
        Stream.Builder<Arguments> argsBuilder = Stream.builder();
        for (String tradingPairPattern : new String[]{"tC", "ET", "Do", "AV", "sh", "HI", "aT"}) {
            argsBuilder.add(Arguments.of(tradingPairPattern));
        }
        return argsBuilder.build();
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/datasets/given/exchange_pairs.yaml")
    void givenNonExistingTradingPairPattern_searchByPattern_shouldReturnExchangePairsForSpecifiedExchange() {
        List<ExchangePair> exchangePairs = exchangePairRepository.searchByPattern(EXCHANGE_NULL_VALUE, NON_EXISTING_TRADING_PAIR_PATTERN_VALUE);
        assertThat(exchangePairs).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideExchangeAndTradingPairPatterns")
    @DataSet("dev/rudyevhenii/crypto_aggregator/exchange_pair/repository/datasets/given/exchange_pairs.yaml")
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

    static Stream<Arguments> provideExchangeAndTradingPairPatterns() {
        Stream.Builder<Arguments> argsBuilder = Stream.builder();
        for (Exchange exchange : Exchange.values()) {
            for (String tradingPairPattern : new String[]{"tC", "ET", "Do", "AV", "sh", "HI", "aT"}) {
                argsBuilder.add(Arguments.of(exchange, tradingPairPattern));
            }
        }
        return argsBuilder.build();
    }

    static class TestResources {
        static final String EMPTY_TRADING_PAIR_VALUE = "";
        static final String NON_EXISTING_TRADING_PAIR_PATTERN_VALUE = "JSDF";

        static final Exchange EXCHANGE_NULL_VALUE = null;

        static final Comparator<ExchangePair> NATURAL_SORTING_ORDER =
                Comparator.comparing((ExchangePair exchangePair) -> exchangePair.getTradingPair().name())
                        .thenComparing(exchangePair -> exchangePair.getExchange().name());
    }
}