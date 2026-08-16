package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.WorkspaceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.WORKSPACE_CACHE;

@Repository
@RequiredArgsConstructor
public class DefaultWorkspaceRepository implements WorkspaceRepository {

    private final SpringDataWorkspaceRepository repository;
    private final WorkspaceEntityMapper mapper;

    @Override
    @CacheEvict(value = WORKSPACE_CACHE, key = "#workspace.userId")
    public Workspace create(Workspace workspace) {
        WorkspaceEntity entity = mapper.toCreateEntity(workspace);
        WorkspaceEntity createdEntity = repository.save(entity);
        return mapper.toDomain(createdEntity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = WORKSPACE_CACHE, key = "#workspace.userId"),
            @CacheEvict(value = WORKSPACE_CACHE, key = "{#workspace.userId, #workspace.id}"),
    })
    public Workspace update(Workspace workspace) {
        WorkspaceEntity entity = mapper.toUpdateEntity(workspace);
        WorkspaceEntity updatedEntity = repository.save(entity);
        return mapper.toDomain(updatedEntity);
    }

    @Override
    @Cacheable(value = WORKSPACE_CACHE, key = "{#userId, #id}", unless = "#result == null")
    public Optional<Workspace> findByUserIdAndId(UUID userId, UUID id) {
        return repository.findByUserIdAndId(userId, id)
                .map(mapper::toDomain);
    }

    @Override
    @Cacheable(value = WORKSPACE_CACHE, key = "#userId")
    public List<Workspace> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = WORKSPACE_CACHE, key = "#userId"),
            @CacheEvict(value = WORKSPACE_CACHE, key = "{#userId, #workspaceId}"),
    })
    public void deleteById(UUID userId, UUID workspaceId) {
        repository.deleteById(workspaceId);
    }

    @Override
    public boolean existsByUserIdAndName(UUID userId, String name) {
        return repository.existsByUserIdAndName(userId, name);
    }

    @Override
    public boolean existsById(UUID workspaceId) {
        return repository.existsById(workspaceId);
    }
}
