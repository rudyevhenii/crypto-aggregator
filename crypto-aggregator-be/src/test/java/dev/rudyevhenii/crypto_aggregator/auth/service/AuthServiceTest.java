package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LogoutRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RefreshTokenRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RegisterRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;
import dev.rudyevhenii.crypto_aggregator.auth.repository.UserRepository;
import dev.rudyevhenii.crypto_aggregator.auth.security.SecurityUserDetails;
import dev.rudyevhenii.crypto_aggregator.core.exception.InvalidJwtTokenException;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.auth.service.AuthServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private GeneratorUtils generator;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void givenRegisterRequest_register_shouldRegisterUser() {
        when(userRepository.existsByEmail(EMIAL)).thenReturn(false);
        when(userRepository.create(any(User.class))).thenReturn(buildSavedUser());
        when(generator.uuid()).thenReturn(ID);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(jwtService.generateAccessToken(buildSavedUser())).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(buildSavedUser())).thenReturn(REFRESH_TOKEN);

        TokenResponseDto result = authService.register(buildRegisterRequest());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).create(captor.capture());

        User user = captor.getValue();
        assertThat(user).isEqualTo(buildSavedUser());
        assertThat(result).isEqualTo(buildTokenResponseDto());
    }

    @Test
    void givenRegisterRequestWithExistingUserEmail_register_shouldThrowException() {
        when(userRepository.existsByEmail(EMIAL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(buildRegisterRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).create(any(User.class));
    }

    @Test
    void givenLoginRequest_login_shouldLoginUser() {
        when(userRepository.existsByEmail(EMIAL)).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(buildAuthentication());
        when(jwtService.generateAccessToken(buildSavedUser())).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(buildSavedUser())).thenReturn(REFRESH_TOKEN);

        TokenResponseDto result = authService.login(buildLoginRequest());

        assertThat(result).isEqualTo(buildTokenResponseDto());
    }

    @Test
    void givenUnauthenticatedUser_login_shouldThrowException() {
        when(userRepository.existsByEmail(EMIAL)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(buildLoginRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void givenRefreshTokenRequest_refreshToken_shouldReturnRenewedTokens() {
        when(jwtService.extractTokenType(REFRESH_TOKEN)).thenReturn(TokenType.REFRESH_TOKEN);
        when(jwtService.extractSubject(REFRESH_TOKEN)).thenReturn(ID);
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildSavedUser()));
        when(jwtService.isTokenValid(REFRESH_TOKEN, buildSavedUser())).thenReturn(true);
        when(jwtService.generateAccessToken(buildSavedUser())).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(buildSavedUser())).thenReturn(REFRESH_TOKEN);

        TokenResponseDto result = authService.refreshToken(buildRefreshTokenRequest());
        assertThat(result).isEqualTo(buildTokenResponseDto());
    }

    @Test
    void givenRefreshTokenRequestWithAccessTokenType_refreshToken_shouldThrowException() {
        when(jwtService.extractTokenType(REFRESH_TOKEN)).thenReturn(TokenType.ACCESS_TOKEN);

        assertThatThrownBy(() -> authService.refreshToken(buildRefreshTokenRequest()))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessageContaining("Expected REFRESH token");

        verify(jwtService, never()).extractSubject(anyString());
    }

    @Test
    void givenNonExistentUserId_refreshToken_shouldThrowException() {
        when(jwtService.extractTokenType(REFRESH_TOKEN)).thenReturn(TokenType.REFRESH_TOKEN);
        when(jwtService.extractSubject(REFRESH_TOKEN)).thenReturn(NON_EXISTENT_ID);

        assertThatThrownBy(() -> authService.refreshToken(buildRefreshTokenRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User does not exist");

        verify(jwtService, never()).isTokenValid(anyString(), any(User.class));
    }

    @Test
    void givenExpiredToken_refreshToken_shouldThrowException() {
        when(jwtService.extractTokenType(REFRESH_TOKEN)).thenReturn(TokenType.REFRESH_TOKEN);
        when(jwtService.extractSubject(REFRESH_TOKEN)).thenReturn(ID);
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildSavedUser()));
        when(jwtService.isTokenValid(REFRESH_TOKEN, buildSavedUser())).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(buildRefreshTokenRequest()))
                .isInstanceOf(JwtTokenExpirationException.class)
                .hasMessageContaining("Token is invalid for this user");

        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void givenLogoutRequest_logout_shouldBlacklistTokens() {
        when(jwtService.extractExpiration(ACCESS_TOKEN)).thenReturn(ACCESS_TOKEN_EXPIRATION);
        when(jwtService.extractExpiration(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_EXPIRATION);
        when(generator.now()).thenReturn(NOW);

        authService.logout(buildLogoutRequest());

        verify(tokenBlacklistService).blacklist(eq(ACCESS_TOKEN), any(Duration.class));
        verify(tokenBlacklistService).blacklist(eq(REFRESH_TOKEN), any(Duration.class));
    }

    static class TestResources {

        static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String EMIAL = "john@gmail.com";
        static final String PASSWORD = "password12345";
        static final String FIRST_NAME = "John";
        static final String LAST_NAME = "Doe";

        static final UUID NON_EXISTENT_ID = UUID.fromString("1aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");

        static final String HASHED_PASSWORD = "{bcrypt}$2a$10$vFJRivCqsolXJT//0mqpkeClTSfr0JcyscC07atrqewjljUVTGET.";

        static final String ACCESS_TOKEN = "access-token";
        static final String REFRESH_TOKEN = "refresh-token";

        static final Instant NOW = Instant.parse("2026-08-01T12:00:00Z");
        static final Date ACCESS_TOKEN_EXPIRATION = Date.from(Instant.parse("2026-08-01T13:00:00Z"));
        static final Date REFRESH_TOKEN_EXPIRATION = Date.from(Instant.parse("2026-08-08T12:00:00Z"));

        static RegisterRequest buildRegisterRequest() {
            return RegisterRequest.builder()
                    .email(EMIAL)
                    .password(PASSWORD)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();
        }

        static User buildSavedUser() {
            return User.builder()
                    .id(ID)
                    .email(EMIAL)
                    .password(HASHED_PASSWORD)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();
        }

        static TokenResponseDto buildTokenResponseDto() {
            return TokenResponseDto.builder()
                    .accessToken(ACCESS_TOKEN)
                    .refreshToken(REFRESH_TOKEN)
                    .build();
        }

        static SecurityUserDetails buildSecurityUserDetails() {
            return new SecurityUserDetails(buildSavedUser());
        }

        static Authentication buildAuthentication() {
            return UsernamePasswordAuthenticationToken.authenticated(
                    buildSecurityUserDetails(),
                    null,
                    Collections.emptyList()
            );
        }

        static LoginRequest buildLoginRequest() {
            return LoginRequest.builder()
                    .email(EMIAL)
                    .password(PASSWORD)
                    .build();
        }

        static RefreshTokenRequest buildRefreshTokenRequest() {
            return RefreshTokenRequest.builder()
                    .refreshToken(REFRESH_TOKEN)
                    .build();
        }

        static LogoutRequest buildLogoutRequest() {
            return LogoutRequest.builder()
                    .accessToken(ACCESS_TOKEN).refreshToken(REFRESH_TOKEN)
                    .build();
        }
    }
}