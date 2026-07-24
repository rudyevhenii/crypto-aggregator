package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    Workspace create(WorkspaceRequest request);

    Workspace update(UUID workspaceId, WorkspaceRequest request);

    Workspace getWorkspaceById(UUID workspaceId);

    List<Workspace> getAllWorkspaces();

    void deleteById(UUID workspaceId);
}
