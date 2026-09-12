package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.chart_widget.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertEntity;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertEntityMapper {

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "true")
    PriceAlertEntity toCreateEntity(PriceAlert priceAlert);

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "false")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PriceAlertEntity toUpdateEntity(PriceAlert priceAlert);

    PriceAlert toDomain(PriceAlertEntity priceAlertEntity);
}
