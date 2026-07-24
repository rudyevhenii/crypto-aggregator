package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataWorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    Optional<WorkspaceEntity> findByUserIdAndId(UUID userId, UUID id);

    List<WorkspaceEntity> findAllByUserId(UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndId(UUID userId, UUID id);
}
