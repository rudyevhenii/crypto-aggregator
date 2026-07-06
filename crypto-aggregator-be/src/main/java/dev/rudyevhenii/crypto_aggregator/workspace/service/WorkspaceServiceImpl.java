package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.auth.repository.UserRepository;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.WorkspaceEntity;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.WorkspaceEntityMapper;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceEntityMapper mapper;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public Workspace create(UUID userId, WorkspaceRequest request) {
        validateUniqueWorkspaceName(userId, request.name());
        Workspace workspace = toDomain(request);
        WorkspaceEntity createEntity = mapper.toCreateEntity(workspace);
        createEntity.setUser(userRepository.getReferenceById(userId));
        log.info("User [{}] created a new workspace", userId);
        return mapper.toDomain(workspaceRepository.save(createEntity));
    }

    @Override
    @Transactional
    public Workspace update(UUID userId, UUID workspaceId, WorkspaceRequest request) {
        validateUniqueWorkspaceName(userId, request.name());
        WorkspaceEntity workspaceEntity = findById(userId, workspaceId);
        updateWorkspace(request, workspaceEntity);
        log.info("User [{}] updated workspace [{}]", userId, workspaceId);
        return mapper.toDomain(workspaceEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceDetail getWorkspaceById(UUID userId, UUID workspaceId) {
        WorkspaceEntity workspaceEntity = findById(userId, workspaceId);
        log.info("User [{}] retrieved workspace [{}] with chart widgets", userId, workspaceId);
        return mapper.toDomainDetail(workspaceEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getAllWorkspaces(UUID userId) {
        log.info("User [{}] getting all workspaces", userId);
        return workspaceRepository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID userId, UUID workspaceId) {
        validateWorkspaceExists(userId, workspaceId);
        log.info("User [{}] deleting workspace [{}]", userId, workspaceId);
        workspaceRepository.deleteById(workspaceId);
    }

    private WorkspaceEntity findById(UUID userId, UUID workspaceId) {
        return workspaceRepository.findByUserIdAndId(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: '%s'"
                        .formatted(workspaceId)));
    }

    private void updateWorkspace(WorkspaceRequest request, WorkspaceEntity workspaceEntity) {
        mapper.toUpdateEntity(request, workspaceEntity);
        workspaceEntity.setUpdatedAt(generator.now());
    }

    private void validateWorkspaceExists(UUID userId, UUID workspaceId) {
        if (!workspaceRepository.existsByUserIdAndId(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: '%s'".formatted(workspaceId));
        }
    }

    private void validateUniqueWorkspaceName(UUID userId, String name) {
        if (workspaceRepository.existsByUserIdAndName(userId, name)) {
            throw new ResourceAlreadyExistsException("Workspace with name '%s' already exists"
                    .formatted(name));
        }
    }

    private Workspace toDomain(WorkspaceRequest request) {
        return Workspace.builder()
                .id(generator.uuid())
                .name(request.name())
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
