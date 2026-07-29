package dev.rudyevhenii.crypto_aggregator.exchange_pair.service;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.repository.ExchangePairRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.exchange_pair.service.ExchangePairServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangePairServiceTest {

    @Mock
    private ExchangePairRepository repository;

    @InjectMocks
    private ExchangePairServiceImpl service;

    @Test
    void givenNothing_findAllTradingPairs_shouldReturnAllExchangePairs() {
        List<ExchangePair> expectedExchangePairs = buildExchangePairList();
        when(repository.findAllExchangePairs()).thenReturn(expectedExchangePairs);

        List<ExchangePair> result = service.findAllExchangePairs();

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedExchangePairs);
        verify(repository).findAllExchangePairs();
    }

    @Test
    void givenNullExchangeAndTradingPair_searchByPattern_shouldReturnAllExchangePairs() {
        List<ExchangePair> expectedExchangePairs = buildExchangePairList();
        when(repository.searchByPattern(EXCHANGE_NULL_VALUE, TRADING_PAIR_PATTERN_NULL_VALUE)).thenReturn(expectedExchangePairs);

        List<ExchangePair> result = service.searchByPattern(EXCHANGE_NULL_VALUE, TRADING_PAIR_PATTERN_NULL_VALUE);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedExchangePairs);
        verify(repository).searchByPattern(EXCHANGE_NULL_VALUE, TRADING_PAIR_PATTERN_NULL_VALUE);
    }

    @Test
    void givenExchange_searchByPattern_shouldReturnExchangePairsForExchange() {
        List<ExchangePair> expectedExchangePairs = buildExchangePairWithExchangeAnd_BTC_TradingPairList();
        when(repository.searchByPattern(EXCHANGE_BINANCE, TRADING_PAIR_BTC_PATTERN)).thenReturn(expectedExchangePairs);

        List<ExchangePair> result = service.searchByPattern(EXCHANGE_BINANCE, TRADING_PAIR_BTC_PATTERN);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedExchangePairs);
        verify(repository).searchByPattern(EXCHANGE_BINANCE, TRADING_PAIR_BTC_PATTERN);
    }

    static class TestResources {

        static final Exchange EXCHANGE_BINANCE = Exchange.BINANCE;
        static final String TRADING_PAIR_BTC_PATTERN = "BTC";

        static final String TRADING_PAIR_PATTERN_NULL_VALUE = null;
        static final Exchange EXCHANGE_NULL_VALUE = null;

        static List<ExchangePair> buildExchangePairList() {
            List<ExchangePair> exchangePairs = new ArrayList<>();
            for (TradingPair tradingPair : TradingPair.values()) {
                for (Exchange exchange : Exchange.values()) {
                    exchangePairs.add(buildExchangePair(tradingPair, exchange));
                }
            }
            return exchangePairs;
        }

        static List<ExchangePair> buildExchangePairWithExchangeAnd_BTC_TradingPairList() {
            return List.of(
                    buildExchangePair(TradingPair.BTC_USD, EXCHANGE_BINANCE)
            );
        }

        static ExchangePair buildExchangePair(TradingPair tradingPair, Exchange exchange) {
            return ExchangePair.builder()
                    .id(UUID.randomUUID())
                    .tradingPair(tradingPair)
                    .exchange(exchange)
                    .build();
        }
    }
}