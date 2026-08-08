package dev.rudyevhenii.crypto_aggregator.workspace.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.workspace.repository.DefaultWorkspaceRepositoryTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class DefaultWorkspaceRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private DefaultWorkspaceRepository repository;

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml")
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/then/created_workspace.yaml")
    void givenWorkspace_create_shouldCreateNewWorkspace() {
        Workspace result = repository.create(buildFirstWorkspace());
        assertThat(result).isEqualTo(buildFirstWorkspace());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/then/updated_workspace.yaml")
    void givenExistingWorkspace_update_shouldUpdateWorkspace() {
        Workspace result = repository.update(buildUpdatedWorkspace());
        assertThat(result).isEqualTo(buildUpdatedWorkspace());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenUserIdAndId_findByUserIdAndId_shouldFindWorkspace() {
        Optional<Workspace> result = repository.findByUserIdAndId(USER_ID, ID_1);
        assertThat(result).contains(buildFirstWorkspace());
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentIds")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenNonExistentUserIdAndId_findByUserIdAndId_shouldReturnEmptyOptional(UUID userId, UUID id) {
        Optional<Workspace> result = repository.findByUserIdAndId(userId, id);
        assertThat(result).isEmpty();
    }

    static Stream<Arguments> provideNonExistentIds() {
        return Stream.of(
                Arguments.of(NON_EXISTENT_USER_ID, NON_EXISTENT_ID),
                Arguments.of(USER_ID, NON_EXISTENT_ID),
                Arguments.of(NON_EXISTENT_USER_ID, ID_1)
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenUserId_findAllByUserId_shouldReturnWorkspaces() {
        List<Workspace> result = repository.findAllByUserId(USER_ID);
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(buildWorkspaceList());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/then/deleted_workspace.yaml")
    void givenUserIdAndId_deleteById_shouldDeleteWorkspace() {
        repository.deleteById(USER_ID, ID_1);
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenUserIdAndName_existsByUserIdAndName_shouldReturnTrue() {
        boolean result = repository.existsByUserIdAndName(USER_ID, NAME_1);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentUserIdAndName")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenNonExistentUserIdAndName_existsByUserIdAndName_shouldReturnFalse(UUID userId, String name) {
        boolean result = repository.existsByUserIdAndName(userId, name);
        assertThat(result).isFalse();
    }

    static Stream<Arguments> provideNonExistentUserIdAndName() {
        return Stream.of(
                Arguments.of(NON_EXISTENT_USER_ID, NON_EXISTENT_NAME),
                Arguments.of(USER_ID, NON_EXISTENT_NAME),
                Arguments.of(NON_EXISTENT_USER_ID, NAME_1)
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenUserIdAndId_existsByUserIdAndId_shouldReturnTrue() {
        boolean result = repository.existsByUserIdAndId(USER_ID, ID_1);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideNonExistentUserIdAndId")
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/repository/datasets/given/workspace.yaml"
    })
    void givenNonExistentUserIdAndId_existsByUserIdAndId_shouldReturnFalse(UUID userId, UUID id) {
        boolean result = repository.existsByUserIdAndId(userId, id);
        assertThat(result).isFalse();
    }

    static Stream<Arguments> provideNonExistentUserIdAndId() {
        return Stream.of(
                Arguments.of(NON_EXISTENT_USER_ID, NON_EXISTENT_ID),
                Arguments.of(USER_ID, NON_EXISTENT_ID),
                Arguments.of(NON_EXISTENT_USER_ID, ID_1)
        );
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

        static final UUID NON_EXISTENT_ID = UUID.fromString("9aaaaaaa-9999-9999-9999-aaaaaaaaaaa9");
        static final UUID NON_EXISTENT_USER_ID = UUID.fromString("9bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb9");
        static final String NON_EXISTENT_NAME = "Non Existent Workspace";

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