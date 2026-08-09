package dev.rudyevhenii.crypto_aggregator.workspace.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.utils.TestUtils.readResource;
import static dev.rudyevhenii.crypto_aggregator.workspace.controller.WorkspaceControllerTest.TestResources.*;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class WorkspaceControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private GeneratorUtils generator;

    @MockitoBean
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml")
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/then/created_workspace.yaml")
    void givenWorkspaceRequest_createWorkspace_shouldCreateNewWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.uuid()).thenReturn(ID);
        when(generator.now()).thenReturn(NOW, CREATED_AT, CREATED_AT);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildWorkspaceRequestJson())
                .when()
                .post(BASE_WORKSPACE_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/createWorkspace_response_createdWorkspace.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml")
    void givenInvalidWorkspaceRequest_createWorkspace_shouldReturnStatusBadRequest() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildRegisterRequestWithInvalidNameJson(null))
                .when()
                .post(BASE_WORKSPACE_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    void givenNonUniqueWorkspaceName_createWorkspace_shouldReturnStatusConflict() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/createWorkspace_request_nonUniqueWorkspaceName.json"))
                .when()
                .post(BASE_WORKSPACE_URL)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/then/updated_workspace.yaml")
    void givenIdAndWorkspaceRequest_updateWorkspace_shouldUpdateWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW, UPDATED_AT);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/updateWorkspace_request_updateWorkspaceName.json"))
                .when()
                .patch(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/updateWorkspace_response_updatedWorkspace.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml")
    void givenInvalidWorkspaceRequest_updateWorkspace_shouldReturnStatusBadRequest() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(buildRegisterRequestWithInvalidNameJson(null))
                .when()
                .patch(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/empty_workspace.yaml"
    })
    void givenNonExistentWorkspaceId_updateWorkspace_shouldReturnStatusNotFound() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/updateWorkspace_request_updateWorkspaceName.json"))
                .when()
                .patch(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    void givenNonUniqueWorkspaceRequest_updateWorkspace_shouldReturnStatusConflict() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .contentType(ContentType.JSON)
                .body(readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/updateWorkspace_request_nonUniqueWorkspaceName.json"))
                .when()
                .patch(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    void givenWorkspaceId_getWorkspaceById_shouldReturnWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/getWorkspace_response_foundWorkspace.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml")
    void givenInvalidWorkspaceId_getWorkspaceById_shouldReturnStatusBadRequest() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_WORKSPACE_URL + "/{workspaceId}", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/empty_workspace.yaml"
    })
    void givenNonExistentWorkspaceId_getWorkspaceById_shouldReturnStatusNotFound() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SneakyThrows
    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    void givenNothing_getAllWorkspaces_shouldReturnAllWorkspaces() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        String actualResponse = given()
                .header(AUTH_HEADER)
                .when()
                .get(BASE_WORKSPACE_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .asString();

        JSONAssert.assertEquals(
                readResource("dev/rudyevhenii/crypto_aggregator/workspace/controller/json/getAllWorkspaces_response_allWorkspaces.json"),
                actualResponse,
                JSONCompareMode.STRICT
        );
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/workspace.yaml"
    })
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/then/deleted_workspace.yaml")
    void givenWorkspaceId_deleteWorkspace_shouldDeleteWorkspace() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml")
    void givenInvalidWorkspaceId_deleteWorkspace_shouldReturnBadRequest() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_WORKSPACE_URL + "/{workspaceId}", NON_VALID_UUID)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DataSet({
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/user.yaml",
            "dev/rudyevhenii/crypto_aggregator/workspace/controller/datasets/given/empty_workspace.yaml"
    })
    void givenWorkspaceId_deleteWorkspace_shouldReturnStatusNotFound() {
        when(userContext.getUserId()).thenReturn(USER_ID);
        when(generator.now()).thenReturn(NOW);

        given()
                .header(AUTH_HEADER)
                .when()
                .delete(BASE_WORKSPACE_URL + "/{workspaceId}", ID)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    static class TestResources {
        static final String BASE_WORKSPACE_URL = "/api/workspaces";

        static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        static final String NAME = "New Workspace";

        static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String NON_VALID_UUID = "not-a-valid-uuid";

        static final Instant NOW = Instant.now();

        static final Instant CREATED_AT = Instant.parse("2026-08-08T12:00:00Z");
        static final Instant UPDATED_AT = Instant.parse("2026-08-10T12:00:00Z");

        static final Header AUTH_HEADER = JwtTokenUtils.buildAuthHeader(USER_ID);

        static String buildWorkspaceRequestJson() {
            return buildWorkspaceRequestJson(NAME);
        }

        static String buildRegisterRequestWithInvalidNameJson(String name) {
            return buildWorkspaceRequestJson(name);
        }

        static String buildWorkspaceRequestJson(String name) {
            return """
                    {
                      "name": %s
                    }
                    """.formatted(toJsonValue(name));
        }

        private static String toJsonValue(String value) {
            return value == null ? "null" : "\"%s\"".formatted(value);
        }
    }
}