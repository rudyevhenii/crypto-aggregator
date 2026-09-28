package dev.rudyevhenii.crypto_aggregator.exchange.metadata.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ChartIntervalRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ExchangeMetadataRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.metadata.TradingPairRqDto;
import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.metadata.model.ExchangeMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeMetadataMapper {

    Exchange map(ExchangeRqDto exchangeRqDto);

    ExchangeRqDto map(Exchange exchange);

    ChartIntervalRqDto map(ChartInterval chartInterval);

    TradingPairRqDto map(TradingPair tradingPair);

    ExchangeMetadataRqDto map(ExchangeMetadataDto exchangeMetadataDto);
}
