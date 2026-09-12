package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.workspace.WorkspaceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.workspace.WorkspaceRqDto;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkspaceMapper {

    WorkspaceRequest map(WorkspaceRequestRqDto rqDto);

    WorkspaceRqDto map(Workspace workspace);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
