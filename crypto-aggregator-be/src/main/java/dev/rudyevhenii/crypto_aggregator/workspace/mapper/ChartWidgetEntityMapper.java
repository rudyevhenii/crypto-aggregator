package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChartWidgetEntityMapper {

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "true")
    ChartWidgetEntity toCreateEntity(ChartWidget chartWidget);

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "false")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ChartWidgetEntity toUpdateEntity(ChartWidget chartWidget);

    ChartWidget toDomain(ChartWidgetEntity chartWidgetEntity);
}
