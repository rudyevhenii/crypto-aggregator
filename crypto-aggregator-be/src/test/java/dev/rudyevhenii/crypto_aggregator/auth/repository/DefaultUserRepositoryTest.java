package dev.rudyevhenii.crypto_aggregator.auth.repository;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import dev.rudyevhenii.crypto_aggregator.AbstractIntegrationTest;
import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.auth.repository.DefaultUserRepositoryTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@DBRider
@DBUnit(
        caseSensitiveTableNames = true,
        alwaysCleanBefore = true,
        alwaysCleanAfter = true,
        escapePattern = "\"?\""
)
class DefaultUserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private SpringDataUserRepository repositorySpy;

    @Test
    @ExpectedDataSet("dev/rudyevhenii/crypto_aggregator/auth/repository/datasets/then/saved_user.yaml")
    void givenNewUser_create_shouldSaveAndReturnUser() {
        User user = userRepository.create(buildUser());
        assertThat(user).isEqualTo(buildUser());
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/repository/datasets/given/user.yaml")
    void givenUserId_findById_shouldReturnUser() {
        Optional<User> result = userRepository.findById(ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(buildUser());
        verify(repositorySpy).findById(ID);

        assertThat(userRepository.findById(ID)).isPresent();
        verifyNoMoreInteractions(repositorySpy);
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/repository/datasets/given/user.yaml")
    void givenUserEmail_findByEmail_shouldReturnUser() {
        Optional<User> result = userRepository.findByEmail(EMIAL);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(buildUser());
        verify(repositorySpy).findByEmail(EMIAL);
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/repository/datasets/given/user.yaml")
    void givenUserEmail_existsByEmail_shouldReturnTrue() {
        boolean result = userRepository.existsByEmail(EMIAL);

        assertThat(result).isTrue();
        verify(repositorySpy).existsByEmail(EMIAL);
    }

    @Test
    @DataSet("dev/rudyevhenii/crypto_aggregator/auth/repository/datasets/given/user.yaml")
    void givenNonExistentUserEmail_existsByEmail_shouldReturnFalse() {
        boolean result = userRepository.existsByEmail(NON_EXISTENT_EMAIL);

        assertThat(result).isFalse();
        verify(repositorySpy).existsByEmail(NON_EXISTENT_EMAIL);
    }

    static class TestResources {

        static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String EMIAL = "john@gmail.com";
        static final String PASSWORD = "password12345";
        static final String FIRST_NAME = "John";
        static final String LAST_NAME = "Doe";

        static final String NON_EXISTENT_EMAIL = "non_existent_email@gmail.com";

        static User buildUser() {
            return User.builder()
                    .id(ID)
                    .email(EMIAL)
                    .password(PASSWORD)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();
        }
    }
}