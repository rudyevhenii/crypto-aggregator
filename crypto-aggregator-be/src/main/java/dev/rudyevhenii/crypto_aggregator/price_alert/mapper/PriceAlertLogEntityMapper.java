package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertLogEntity;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertLogEntityMapper {

    @Mapping(target = PriceAlertLogEntity.Fields.newEntity, constant = "true")
    PriceAlertLogEntity toCreateEntity(PriceAlertLog priceAlertLog);

    PriceAlertLog toDomain(PriceAlertLogEntity priceAlertLog);
}
