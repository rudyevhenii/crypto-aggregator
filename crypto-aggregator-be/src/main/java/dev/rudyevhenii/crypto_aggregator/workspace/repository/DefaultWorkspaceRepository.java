package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import dev.rudyevhenii.crypto_aggregator.user.repository.SpringDataUserRepository;
import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.WorkspaceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultWorkspaceRepository implements WorkspaceRepository {

    private final SpringDataWorkspaceRepository workspaceRepository;
    private final SpringDataUserRepository userRepository;
    private final WorkspaceEntityMapper mapper;

    @Override
    public Workspace create(UUID userId, Workspace workspace) {
        WorkspaceEntity createEntity = mapper.toCreateEntity(workspace, userId);
        createEntity.setUser(userRepository.getReferenceById(userId));
        WorkspaceEntity savedEntity = workspaceRepository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Workspace update(UUID userId, Workspace workspace) {
        WorkspaceEntity updateEntity = mapper.toUpdateEntity(workspace, userId);
        updateEntity.setUser(userRepository.getReferenceById(userId));
        WorkspaceEntity savedEntity = workspaceRepository.save(updateEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Workspace> findById(UUID userId, UUID workspaceId) {
        return workspaceRepository.findByUserIdAndId(userId, workspaceId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<WorkspaceDetail> findByIdWithDetail(UUID userId, UUID workspaceId) {
        return workspaceRepository.findByUserIdAndId(userId, workspaceId)
                .map(mapper::toDomainDetail);
    }

    @Override
    public List<Workspace> findAllWorkspaces(UUID userId) {
        return workspaceRepository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID userId, UUID workspaceId) {
        workspaceRepository.deleteByUserIdAndId(userId, workspaceId);
    }

    @Override
    public boolean existsByName(UUID userId, String name) {
        return workspaceRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    public boolean existsById(UUID userId, UUID workspaceId) {
        return workspaceRepository.existsByUserIdAndId(userId, workspaceId);
    }
}
