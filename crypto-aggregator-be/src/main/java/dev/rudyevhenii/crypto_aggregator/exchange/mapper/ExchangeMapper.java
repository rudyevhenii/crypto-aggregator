package dev.rudyevhenii.crypto_aggregator.exchange.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartIntervalRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeMetadataRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.HistoricalPriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.Ticker24hRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.HistoricalPriceRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.historical.model.Ticker24hDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.metadata.model.ExchangeMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeMapper {

    Exchange map(ExchangeRqDto exchangeRqDto);

    ExchangeRqDto map(Exchange exchange);

    TradingPair map(TradingPairRqDto tradingPairRqDto);

    TradingPairRqDto map(TradingPair tradingPair);

    ChartInterval map(ChartIntervalRqDto chartIntervalRqDto);

    ChartIntervalRqDto map(ChartInterval chartInterval);

    ExchangeMetadataRqDto map(ExchangeMetadataDto exchangeMetadataDto);

    HistoricalPriceRequest map(HistoricalPriceRequestRqDto request);

    HistoricalPriceRqDto map(HistoricalPriceDto historicalPriceDto);

    Ticker24hRqDto map(Ticker24hDto ticker24hDto);

    LivePriceRqDto map(LivePriceDto livePriceDto);

    ExchangeHealthRqDto map(ExchangeHealthDto exchangeHealthDto);

    default Instant toInstant(OffsetDateTime endTimeCursor) {
        return endTimeCursor == null ? null : endTimeCursor.toInstant();
    }

    default OffsetDateTime toOffsetDateTime(Instant endTimeCursor) {
        return endTimeCursor == null ? null : endTimeCursor.atOffset(ZoneOffset.UTC);
    }
}
