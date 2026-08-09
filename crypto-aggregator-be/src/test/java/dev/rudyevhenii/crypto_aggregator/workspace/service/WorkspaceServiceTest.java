package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.WorkspaceRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.workspace.service.WorkspaceServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository repository;
    
    @Mock
    private UserContext userContext;
    
    @Mock
    private GeneratorUtils generator;
    
    @InjectMocks
    private WorkspaceServiceImpl service;

    @Test
    void givenWorkspaceRequest_create_shouldCreateWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.uuid()).thenReturn(ID_1);
        when(generator.now()).thenReturn(CREATED_AT_1, CREATED_AT_1);
        when(repository.create(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = service.create(buildWorkspaceRequest());

        assertThat(result).isEqualTo(buildFirstWorkspace());
    }

    @Test
    void givenNonUniqueWorkspaceName_create_shouldThrowException() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByUserIdAndName(USER_ID, NAME_1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(buildWorkspaceRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void givenIdAndWorkspaceRequest_update_shouldUpdateWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findByUserIdAndId(USER_ID, ID_1)).thenReturn(Optional.of(buildFirstWorkspace()));
        when(generator.now()).thenReturn(UPDATED_AT_1);
        when(repository.update(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = service.update(ID_1, buildUpdatedWorkspaceRequest());

        assertThat(result).isEqualTo(buildUpdatedWorkspace());
    }

    @Test
    void givenNonUniqueWorkspaceName_update_shouldThrowException() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByUserIdAndName(USER_ID, NAME_1)).thenReturn(true);

        assertThatThrownBy(() -> service.update(ID_1, buildWorkspaceRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void givenNonExistingId_update_shouldThrowException() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findByUserIdAndId(USER_ID, ID_1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ID_1, buildWorkspaceRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenId_getWorkspaceById_shouldReturnWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findByUserIdAndId(USER_ID, ID_1)).thenReturn(Optional.of(buildFirstWorkspace()));

        Workspace result = service.getWorkspaceById(ID_1);
        assertThat(result).isEqualTo(buildFirstWorkspace());
    }

    @Test
    void givenNothing_getAllWorkspaces_shouldReturnAllWorkspaces() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.findAllByUserId(USER_ID)).thenReturn(buildWorkspaceList());

        List<Workspace> result = service.getAllWorkspaces();

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(buildWorkspaceList());
    }

    @Test
    void givenId_deleteById_shouldDeleteWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByUserIdAndId(USER_ID, ID_1)).thenReturn(true);

        service.deleteById(ID_1);
        verify(repository).deleteById(USER_ID, ID_1);
    }

    @Test
    void givenNonExistentWorkspaceId_deleteById_shouldThrowException() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(repository.existsByUserIdAndId(USER_ID, ID_1)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(ID_1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    static class TestResources {
        static final UUID ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        static final String NAME_1 = "New Workspace";
        static final Instant CREATED_AT_1 = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT_1 = Instant.parse("2026-08-10T12:00:00Z");

        static final UUID ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        static final String NAME_2 = "Second Workspace";
        static final Instant CREATED_AT_2 = Instant.parse("2026-08-09T12:00:00Z");

        static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        static final String UPDATED_NAME_1 = "Updated Workspace";

        static WorkspaceRequest buildWorkspaceRequest() {
            return WorkspaceRequest.builder()
                    .name(NAME_1)
                    .build();
        }

        static WorkspaceRequest buildUpdatedWorkspaceRequest() {
            return WorkspaceRequest.builder()
                    .name(UPDATED_NAME_1)
                    .build();
        }

        static List<Workspace> buildWorkspaceList() {
            return List.of(
                    buildFirstWorkspace(),
                    buildSecondWorkspace()
            );
        }

        static Workspace buildFirstWorkspace() {
            return Workspace.builder()
                    .id(ID_1)
                    .name(NAME_1)
                    .userId(USER_ID)
                    .createdAt(CREATED_AT_1)
                    .updatedAt(CREATED_AT_1)
                    .build();
        }

        static Workspace buildSecondWorkspace() {
            return Workspace.builder()
                    .id(ID_2)
                    .name(NAME_2)
                    .userId(USER_ID)
                    .createdAt(CREATED_AT_2)
                    .updatedAt(CREATED_AT_2)
                    .build();
        }

        static Workspace buildUpdatedWorkspace() {
            return Workspace.builder()
                    .id(ID_1)
                    .name(UPDATED_NAME_1)
                    .userId(USER_ID)
                    .createdAt(CREATED_AT_1)
                    .updatedAt(UPDATED_AT_1)
                    .build();
        }
    }
}