package dev.rudyevhenii.crypto_aggregator.workspace;

import dev.rudyevhenii.crypto_aggregator.api.dto.WorkspaceRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.WorkspaceRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.WorkspaceApi;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.WorkspaceMapper;
import dev.rudyevhenii.crypto_aggregator.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkspaceController implements WorkspaceApi {

    private final WorkspaceService workspaceService;
    private final WorkspaceMapper workspaceMapper;

    @Override
    public ResponseEntity<WorkspaceRqDto> createWorkspace(WorkspaceRequestRqDto request) {
        Workspace response = workspaceService.create(workspaceMapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceMapper.map(response));
    }

    @Override
    public ResponseEntity<WorkspaceRqDto> updateWorkspace(UUID workspaceId, WorkspaceRequestRqDto request) {
        Workspace response = workspaceService.update(workspaceId, workspaceMapper.map(request));
        return ResponseEntity.ok(workspaceMapper.map(response));
    }

    @Override
    public ResponseEntity<WorkspaceRqDto> getWorkspaceById(UUID workspaceId) {
        Workspace response = workspaceService.getWorkspaceById(workspaceId);
        return ResponseEntity.ok(workspaceMapper.map(response));
    }

    @Override
    public ResponseEntity<List<WorkspaceRqDto>> getAllWorkspaces() {
        List<Workspace> response = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(response.stream()
                .map(workspaceMapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<Void> deleteWorkspace(UUID workspaceId) {
        workspaceService.deleteById(workspaceId);
        return ResponseEntity.noContent()
                .build();
    }
}
