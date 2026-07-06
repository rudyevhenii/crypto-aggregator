package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.WorkspaceDetailRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.WorkspaceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.WorkspaceRqDto;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;
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

    WorkspaceDetailRqDto map(WorkspaceDetail workspaceDetail);

    default OffsetDateTime toOffsetDateTime(Instant endTimeCursor) {
        return endTimeCursor.atOffset(ZoneOffset.UTC);
    }
}
