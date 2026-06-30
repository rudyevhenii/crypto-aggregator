package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataWorkspaceRepository extends CrudRepository<WorkspaceEntity, UUID> {

    List<WorkspaceEntity> findAllByUserId(UUID userId);

    Optional<WorkspaceEntity> findByUserIdAndId(UUID userId, UUID workspaceId);

    @Modifying
    void deleteByIdAndUserId(UUID userId, UUID workspaceId);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndId(UUID userId, UUID workspaceId);
}
