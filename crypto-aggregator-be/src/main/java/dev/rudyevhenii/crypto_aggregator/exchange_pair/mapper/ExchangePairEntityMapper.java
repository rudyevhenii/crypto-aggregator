package dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper;

import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangePairEntityMapper {

    ExchangePair toDomain(ExchangePairEntity entity);
}
