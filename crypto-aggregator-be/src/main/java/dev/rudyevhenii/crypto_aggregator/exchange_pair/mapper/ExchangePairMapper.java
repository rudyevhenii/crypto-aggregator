package dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangePairRqDto;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangePairMapper {

    ExchangePairRqDto map(ExchangePair exchangePair);
}
