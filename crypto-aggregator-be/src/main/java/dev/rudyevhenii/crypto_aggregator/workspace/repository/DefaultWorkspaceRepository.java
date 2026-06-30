package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.WorkspaceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultWorkspaceRepository implements WorkspaceRepository {

    private final SpringDataWorkspaceRepository repository;
    private final WorkspaceEntityMapper mapper;

    @Override
    public Workspace create(UUID userId, Workspace workspace) {
        WorkspaceEntity createEntity = mapper.toCreateEntity(workspace, userId);
        WorkspaceEntity savedEntity = repository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Workspace update(UUID userId, Workspace workspace) {
        WorkspaceEntity createEntity = mapper.toUpdateEntity(workspace, userId);
        WorkspaceEntity savedEntity = repository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Workspace> findWorkspaceById(UUID userId, UUID workspaceId) {
        return repository.findByUserIdAndId(userId, workspaceId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Workspace> findAllWorkspaces(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID userId, UUID workspaceId) {
        repository.deleteByIdAndUserId(userId, workspaceId);
    }

    @Override
    public boolean existsByName(UUID userId, String name) {
        return repository.existsByUserIdAndName(userId, name);
    }

    @Override
    public boolean existsById(UUID userId, UUID workspaceId) {
        return repository.existsByUserIdAndId(userId, workspaceId);
    }
}
