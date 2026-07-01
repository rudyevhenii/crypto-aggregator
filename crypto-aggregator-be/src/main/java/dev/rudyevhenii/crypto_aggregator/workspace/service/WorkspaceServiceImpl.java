package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.WorkspaceDetail;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final ChartWidgetService chartWidgetService;
    private final GeneratorUtils generator;

    @Override
    public Workspace create(UUID userId, WorkspaceRequest request) {
        validateUniqueWorkspaceName(userId, request);
        Workspace workspace = toDomain(request);
        return workspaceRepository.create(userId, workspace);
    }

    @Override
    public Workspace update(UUID userId, UUID workspaceId, WorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: %s".formatted(workspaceId)));;
        if (!workspace.getName().equals(request.name())) {
            validateUniqueWorkspaceName(userId, request);
            workspace.setName(request.name());
        }
        workspace.setUpdatedAt(generator.now());
        return workspaceRepository.update(userId, workspace);
    }

    @Override
    public WorkspaceDetail getWorkspaceById(UUID userId, UUID workspaceId) {
        return workspaceRepository.findByIdWithDetail(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: %s".formatted(workspaceId)));
    }

    @Override
    public List<Workspace> getAllWorkspaces(UUID userId) {
        return workspaceRepository.findAllWorkspaces(userId);
    }

    @Override
    public void deleteById(UUID userId, UUID workspaceId) {
        validateWorkspaceExists(userId, workspaceId);
        workspaceRepository.deleteById(userId, workspaceId);
    }

    @Override
    public ChartWidget createChartWidget(UUID userId, UUID workspaceId, ChartWidgetRequest request) {
        validateWorkspaceExists(userId, workspaceId);
        return chartWidgetService.create(workspaceId, request);
    }

    private void validateWorkspaceExists(UUID userId, UUID workspaceId) {
        if (workspaceRepository.existsById(userId, workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with id: %s".formatted(workspaceId));
        }
    }

    private void validateUniqueWorkspaceName(UUID userId, WorkspaceRequest request) {
        if (workspaceRepository.existsByName(userId, request.name())) {
            throw new ResourceAlreadyExistsException("Workspace with name '%s' already exists"
                    .formatted(request.name()));
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
