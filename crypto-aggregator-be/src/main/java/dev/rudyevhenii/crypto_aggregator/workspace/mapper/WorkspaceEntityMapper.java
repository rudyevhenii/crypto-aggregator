package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkspaceEntityMapper {

    @Mapping(target = WorkspaceEntity.Fields.newEntity, constant = "true")
    WorkspaceEntity toCreateEntity(Workspace workspace, UUID userId);

    @Mapping(target = WorkspaceEntity.Fields.newEntity, constant = "false")
    WorkspaceEntity toUpdateEntity(Workspace workspace, UUID userId);

    Workspace toDomain(WorkspaceEntity entity);
}
