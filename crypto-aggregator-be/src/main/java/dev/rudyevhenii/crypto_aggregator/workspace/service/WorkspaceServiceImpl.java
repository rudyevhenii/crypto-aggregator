package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
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

    private final WorkspaceRepository repository;
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public Workspace create(WorkspaceRequest request) {
        validateUniqueWorkspaceName(userContext.getUserId(), request.name());
        Workspace workspace = toDomain(request);

        log.info("User [{}] created a new workspace", userContext.getUserId());
        return repository.create(workspace);
    }

    @Override
    @Transactional
    public Workspace update(UUID workspaceId, WorkspaceRequest request) {
        validateUniqueWorkspaceName(userContext.getUserId(), request.name());
        Workspace workspace = getById(userContext.getUserId(), workspaceId);
        workspace.setUpdatedAt(generator.now());

        log.info("User [{}] updated workspace [{}]", userContext.getUserId(), workspaceId);
        return repository.update(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getWorkspaceById(UUID workspaceId) {
        Workspace workspace = getById(userContext.getUserId(), workspaceId);

        log.info("User [{}] retrieved workspace [{}] with chart widgets", userContext.getUserId(), workspaceId);
        return workspace;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getAllWorkspaces() {
        log.info("User [{}] getting all workspaces", userContext.getUserId());
        return repository.findAllByUserId(userContext.getUserId());
    }

    @Override
    @Transactional
    public void deleteById(UUID workspaceId) {
        validateWorkspaceExists(userContext.getUserId(), workspaceId);
        log.info("User [{}] deleting workspace [{}]", userContext.getUserId(), workspaceId);
        repository.deleteById(userContext.getUserId(), workspaceId);
    }

    private Workspace getById(UUID userId, UUID workspaceId) {
        return repository.findByUserIdAndId(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: '%s'"
                        .formatted(workspaceId)));
    }
    private void validateWorkspaceExists(UUID userId, UUID workspaceId) {
        if (!repository.existsByUserIdAndId(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: '%s'".formatted(workspaceId));
        }
    }

    private void validateUniqueWorkspaceName(UUID userId, String name) {
        if (repository.existsByUserIdAndName(userId, name)) {
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
