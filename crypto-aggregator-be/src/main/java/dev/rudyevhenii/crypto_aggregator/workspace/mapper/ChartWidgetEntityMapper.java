package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.workspace.ChartWidgetEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChartWidgetEntityMapper {

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "true")
    ChartWidgetEntity toCreateEntity(ChartWidget chartWidget, UUID workspaceId);

    @Mapping(target = ChartWidgetEntity.Fields.newEntity, constant = "false")
    ChartWidgetEntity toUpdateEntity(ChartWidget chartWidget, UUID workspaceId);

    ChartWidget toDomain(ChartWidgetEntity chartWidgetEntity);
}
