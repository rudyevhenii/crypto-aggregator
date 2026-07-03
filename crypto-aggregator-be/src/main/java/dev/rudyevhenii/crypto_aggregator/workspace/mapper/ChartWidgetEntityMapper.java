package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChartWidgetEntityMapper {

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "true")
    ChartWidgetEntity toCreateEntity(ChartWidget chartWidget);

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "false")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdateEntity(UpdateChartWidgetRequest request, @MappingTarget ChartWidgetEntity entity);

    @Mappings({
            @Mapping(target = "exchangePairId", source = "exchangePair.id"),
            @Mapping(target = "tradingPair", source = "exchangePair.tradingPair"),
            @Mapping(target = "exchange", source = "exchangePair.exchange")
    })
    ChartWidget toDomain(ChartWidgetEntity chartWidgetEntity);
}
