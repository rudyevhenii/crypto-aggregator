package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataWorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    List<WorkspaceEntity> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"chartWidgets", "chartWidgets.exchangePair"})
    Optional<WorkspaceEntity> findByUserIdAndId(UUID userId, UUID workspaceId);

    @Modifying
    void deleteByUserIdAndId(UUID userId, UUID workspaceId);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndId(UUID userId, UUID workspaceId);
}
