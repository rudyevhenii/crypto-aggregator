package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.auth.service.UserServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void givenUserEmail_loadUserByUsername_shouldLoadUserDetails() {
        when(userRepository.findByEmail(EMIAL)).thenReturn(Optional.of(buildUser()));

        UserDetails result = service.loadUserByUsername(EMIAL);

        assertThat(result.getUsername()).isEqualTo(EMIAL);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        assertThat(result.getAuthorities()).isEmpty();
        verify(userRepository).findByEmail(EMIAL);
    }

    @Test
    void givenNonExistentUserEmail_loadUserByUsername_shouldThrowException() {
        when(userRepository.findByEmail(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername(NON_EXISTENT_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User does not exist with email");
        verify(userRepository).findByEmail(NON_EXISTENT_EMAIL);
    }

    @Test
    void givenUserId_findById_shouldLoadUserDetails() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildUser()));

        UserDetails result = service.findById(ID);

        assertThat(result.getUsername()).isEqualTo(EMIAL);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        assertThat(result.getAuthorities()).isEmpty();
        verify(userRepository).findById(ID);
    }

    @Test
    void givenNonExistentUserId_findById_shouldThrowException() {
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(NON_EXISTENT_ID))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User does not exist with id");
        verify(userRepository).findById(NON_EXISTENT_ID);
    }

    static class TestResources {
        static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String EMIAL = "john@gmail.com";
        static final String PASSWORD = "password12345";
        static final String FIRST_NAME = "John";
        static final String LAST_NAME = "Doe";

        static final UUID NON_EXISTENT_ID = UUID.fromString("1aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
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