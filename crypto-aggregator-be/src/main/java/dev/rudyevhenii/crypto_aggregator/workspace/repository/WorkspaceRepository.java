package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    Workspace create(UUID userId, Workspace workspace);

    Workspace update(UUID userId, Workspace workspace);

    Optional<Workspace> findById(UUID userId, UUID workspaceId);

    Optional<WorkspaceDetail> findByIdWithDetail(UUID userId, UUID workspaceId);

    List<Workspace> findAllWorkspaces(UUID userId);

    void deleteById(UUID userId, UUID workspaceId);

    boolean existsByName(UUID userId, String name);

    boolean existsById(UUID userId, UUID workspaceId);
}
