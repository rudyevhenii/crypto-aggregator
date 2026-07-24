package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ChartWidgetEntityMapper.class})
public interface WorkspaceEntityMapper {

    @Mapping(target = WorkspaceEntity.Fields.newEntity, constant = "true")
    WorkspaceEntity toCreateEntity(Workspace workspace);

    @Mapping(target = WorkspaceEntity.Fields.newEntity, constant = "false")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WorkspaceEntity toUpdateEntity(Workspace workspace);

    Workspace toDomain(WorkspaceEntity entity);
}
