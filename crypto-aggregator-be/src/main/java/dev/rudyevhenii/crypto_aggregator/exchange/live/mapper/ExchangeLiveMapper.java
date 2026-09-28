package dev.rudyevhenii.crypto_aggregator.exchange.live.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.live.ExchangeHealthRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.LivePriceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.live.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeLiveMapper {

    Exchange map(ExchangeRqDto exchangeRqDto);

    TradingPair map(TradingPairRqDto tradingPairRqDto);

    LivePriceRqDto map(LivePriceDto livePriceDto);

    ExchangeHealthRqDto map(ExchangeHealthDto exchangeHealthDto);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
