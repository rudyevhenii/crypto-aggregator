package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository repository;
    private final GeneratorUtils generator;

    @Override
    public Workspace create(UUID userId, WorkspaceRequest request) {
        validateUniqueWorkspaceName(userId, request);
        Workspace workspace = toCreateDomain(request);
        return repository.create(userId, workspace);
    }

    @Override
    public Workspace update(UUID userId, UUID workspaceId, WorkspaceRequest request) {
        Workspace workspace = findById(userId, workspaceId);
        if (!workspace.getName().equals(request.name())) {
            validateUniqueWorkspaceName(userId, request);
            workspace.setName(request.name());
        }
        workspace.setUpdatedAt(generator.now());
        return repository.update(userId, workspace);
    }

    @Override
    public Workspace getWorkspaceById(UUID userId, UUID id) {
        return findById(userId, id);
    }

    @Override
    public List<Workspace> getAllWorkspaces(UUID userId) {
        return repository.findAllWorkspaces(userId);
    }

    @Override
    public void deleteById(UUID userId, UUID workspaceId) {
        if (repository.existsById(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: %s".formatted(workspaceId));
        }
        repository.deleteById(userId, workspaceId);
    }

    private Workspace findById(UUID userId, UUID id) {
        return repository.findWorkspaceById(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: %s".formatted(id)));
    }

    private void validateUniqueWorkspaceName(UUID userId, WorkspaceRequest request) {
        if (repository.existsByName(userId, request.name())) {
            throw new ResourceAlreadyExistsException("Workspace with name '%s' already exists"
                    .formatted(request.name()));
        }
    }

    private Workspace toCreateDomain(WorkspaceRequest request) {
        return Workspace.builder()
                .id(generator.uuid())
                .name(request.name())
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
