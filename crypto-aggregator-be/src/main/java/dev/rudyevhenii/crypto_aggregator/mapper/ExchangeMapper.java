package dev.rudyevhenii.crypto_aggregator.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartIntervalRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeMetadataRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.ExchangeMetadataDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.dto.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeMapper {

    Exchange toDomain(ExchangeRqDto exchangeRqDto);

    ExchangeRqDto toResponse(Exchange exchange);

    TradingPair toDomain(TradingPairRqDto tradingPairRqDto);

    TradingPairRqDto toResponse(TradingPair tradingPair);

    ChartIntervalRqDto toResponse(ChartInterval chartInterval);

    ExchangeMetadataRqDto toResponse(ExchangeMetadataDto exchangeMetadataDto);

    HistoricalPriceRequest toDomain(HistoricalPriceRequestRqDto historicalPriceRequestRqDto);

    HistoricalPriceRqDto toResponse(HistoricalPriceDto historicalPriceDto);

    Ticker24hRqDto toResponse(Ticker24hDto ticker24hDto);

    LivePriceRqDto toResponse(LivePriceDto livePriceDto);

    ExchangeHealthRqDto toResponse(ExchangeHealthDto exchangeHealthDto);

    default Instant toInstant(OffsetDateTime endTimeCursor) {
        return endTimeCursor.toInstant();
    }

    default OffsetDateTime toOffsetDateTime(Instant endTimeCursor) {
        return endTimeCursor.atOffset(ZoneOffset.UTC);
    }
}
