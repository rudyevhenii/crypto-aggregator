package dev.rudyevhenii.crypto_aggregator.exchange.historical.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.historical.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.historical.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeHistoricalMapper {

    Exchange map(ExchangeRqDto exchangeRqDto);

    TradingPair map(TradingPairRqDto tradingPairRqDto);

    HistoricalPriceRequest map(HistoricalPriceRequestRqDto request);

    HistoricalPriceRqDto map(HistoricalPriceDto historicalPriceDto);

    Ticker24hRqDto map(Ticker24hDto ticker24hDto);

    default Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
