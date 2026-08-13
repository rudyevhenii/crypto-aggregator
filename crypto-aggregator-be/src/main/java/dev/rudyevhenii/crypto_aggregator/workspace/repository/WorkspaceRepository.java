package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    Workspace create(Workspace workspace);

    Workspace update(Workspace workspace);

    Optional<Workspace> findByUserIdAndId(UUID userId, UUID id);

    List<Workspace> findAllByUserId(UUID userId);

    void deleteById(UUID userId, UUID workspaceId);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndId(UUID userId, UUID id);

    boolean existsById(UUID workspaceId);
}
