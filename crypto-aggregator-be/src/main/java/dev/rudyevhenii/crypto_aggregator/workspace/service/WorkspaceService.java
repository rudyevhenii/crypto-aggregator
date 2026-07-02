package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    Workspace create(UUID userId, WorkspaceRequest request);

    Workspace update(UUID userId, UUID workspaceId, WorkspaceRequest request);

    WorkspaceDetail getWorkspaceById(UUID userId, UUID workspaceId);

    List<Workspace> getAllWorkspaces(UUID userId);

    void deleteById(UUID userId, UUID workspaceId);

}
