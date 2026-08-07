package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.core.exception.InvalidJwtTokenException;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.rudyevhenii.crypto_aggregator.auth.service.JwtServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private GeneratorUtils generator;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(
                ACCESS_TOKEN_EXP_MS,
                REFRESH_TOKEN_EXP_MS,
                JwtTokenUtils.SECRET_KEY,
                generator
        );
    }

    @Test
    void givenUser_generateAccessToken_shouldReturnAccessToken() {
        when(generator.now()).thenReturn(NOW);

        String accessToken = jwtService.generateAccessToken(buildFirstUser());

        assertThat(jwtService.extractSubject(accessToken)).isEqualTo(ID_1);
        assertThat(jwtService.extractTokenType(accessToken)).isEqualTo(TokenType.ACCESS_TOKEN);
        assertThat(jwtService.extractExpiration(accessToken))
                .isCloseTo(ACCESS_TOKEN_EXP, Duration.ofSeconds(1).toMillis());
    }

    @Test
    void givenUser_generateRefreshToken_shouldReturnRefreshToken() {
        when(generator.now()).thenReturn(NOW);

        String refreshToken = jwtService.generateRefreshToken(buildFirstUser());

        assertThat(jwtService.extractSubject(refreshToken)).isEqualTo(ID_1);
        assertThat(jwtService.extractTokenType(refreshToken)).isEqualTo(TokenType.REFRESH_TOKEN);
        assertThat(jwtService.extractExpiration(refreshToken))
                .isCloseTo(REFRESH_TOKEN_EXP, Duration.ofSeconds(1).toMillis());
    }

    @Test
    void givenValidToken_extractSubject_shouldReturnSubject() {
        String accessToken = JwtTokenUtils.buildAccessToken(ID_1, ACCESS_TOKEN_EXP);

        UUID result = jwtService.extractSubject(accessToken);

        assertThat(result).isEqualTo(ID_1);
    }

    @Test
    void givenExpiredToken_extractSubject_shouldThrowException() {
        String accessToken = JwtTokenUtils.buildExpiredAccessToken(ID_1);

        assertThatThrownBy(() -> jwtService.extractSubject(accessToken))
                .isInstanceOf(JwtTokenExpirationException.class)
                .hasMessageContaining("Jwt token has expired");
    }

    @Test
    void givenCorruptedToken_extractSubject_shouldThrowException() {
        String accessToken = JwtTokenUtils.buildCorruptedAccessToken(ID_1, ACCESS_TOKEN_EXP);

        assertThatThrownBy(() -> jwtService.extractSubject(accessToken))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessageContaining("Invalid Jwt token");
    }

    @Test
    void givenValidToken_extractExpiration_shouldReturnExpiration() {
        String accessToken = JwtTokenUtils.buildAccessToken(ID_1, ACCESS_TOKEN_EXP);

        Date result = jwtService.extractExpiration(accessToken);

        assertThat(result).isCloseTo(ACCESS_TOKEN_EXP, Duration.ofSeconds(1).toMillis());
    }

    @Test
    void givenExpiredToken_extractExpiration_shouldThrowException() {
        String accessToken = JwtTokenUtils.buildExpiredAccessToken(ID_1);

        assertThatThrownBy(() -> jwtService.extractExpiration(accessToken))
                .isInstanceOf(JwtTokenExpirationException.class)
                .hasMessageContaining("Jwt token has expired");
    }

    @Test
    void givenCorruptedToken_extractExpiration_shouldThrowException() {
        String accessToken = JwtTokenUtils.buildCorruptedAccessToken(ID_1, ACCESS_TOKEN_EXP);

        assertThatThrownBy(() -> jwtService.extractExpiration(accessToken))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessageContaining("Invalid Jwt token");
    }

    @ParameterizedTest
    @MethodSource("provideValidTokens")
    void givenValidTokens_extractTokenType_shouldReturnTokenType(String token, TokenType tokenType) {
        TokenType result = jwtService.extractTokenType(token);

        assertThat(result).isEqualTo(tokenType);
    }

    static Stream<Arguments> provideValidTokens() {
        return Stream.of(
                Arguments.of(JwtTokenUtils.buildAccessToken(ID_1, ACCESS_TOKEN_EXP),
                        TokenType.ACCESS_TOKEN),
                Arguments.of(JwtTokenUtils.buildRefreshToken(ID_1, REFRESH_TOKEN_EXP),
                        TokenType.REFRESH_TOKEN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExpiredTokens")
    void givenExpiredTokens_extractTokenType_shouldThrowException(String token) {
        assertThatThrownBy(() -> jwtService.extractTokenType(token))
                .isInstanceOf(JwtTokenExpirationException.class)
                .hasMessageContaining("Jwt token has expired");
    }

    static Stream<Arguments> provideExpiredTokens() {
        return Stream.of(
                Arguments.of(JwtTokenUtils.buildExpiredAccessToken(ID_1)),
                Arguments.of(JwtTokenUtils.buildExpiredRefreshToken(ID_1))
        );
    }

    @ParameterizedTest
    @MethodSource("provideCorruptedTokens")
    void givenCorruptedTokens_extractTokenType_shouldThrowException(String token) {
        assertThatThrownBy(() -> jwtService.extractTokenType(token))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessageContaining("Invalid Jwt token");
    }

    static Stream<Arguments> provideCorruptedTokens() {
        return Stream.of(
                Arguments.of(JwtTokenUtils.buildCorruptedAccessToken(ID_1, ACCESS_TOKEN_EXP)),
                Arguments.of(JwtTokenUtils.buildCorruptedRefreshToken(ID_1, REFRESH_TOKEN_EXP))
        );
    }

    @Test
    void givenValidAccessTokenWithUser_isTokenValid_shouldReturnTrue() {
        when(generator.now()).thenReturn(NOW);
        String accessToken = JwtTokenUtils.buildAccessToken(ID_1, ACCESS_TOKEN_EXP);

        boolean result = jwtService.isTokenValid(accessToken, buildFirstUser());

        assertThat(result).isTrue();
    }

    @Test
    void givenExpiredAccessTokenWithUser_isTokenValid_shouldReturnFalse() {
        String accessToken = JwtTokenUtils.buildExpiredAccessToken(ID_1);

        boolean result = jwtService.isTokenValid(accessToken, buildFirstUser());

        assertThat(result).isFalse();
    }

    @Test
    void givenValidTokenWithAnotherUser_isTokenValid_shouldReturnFalse() {
        when(generator.now()).thenReturn(NOW);
        String accessToken = JwtTokenUtils.buildAccessToken(ID_1, ACCESS_TOKEN_EXP);

        boolean result = jwtService.isTokenValid(accessToken, buildSecondUser());

        assertThat(result).isFalse();
    }

    @Test
    void givenCorruptedTokenWithUser_isTokenValid_shouldReturnFalse() {
        String token = JwtTokenUtils.buildCorruptedAccessToken(ID_1, ACCESS_TOKEN_EXP);

        boolean result = jwtService.isTokenValid(token, buildFirstUser());

        assertThat(result).isFalse();
    }

    static class TestResources {

        static final UUID ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        static final String EMAIL_1 = "JohnDoe@gmail.com";
        static final String HASHED_PASSWORD_1 = "{bcrypt}$2a$10$vFJRivCqsolXJT//0mqpkeClTSfr0JcyscC07atrqewjljUVTGET.";
        static final String FIRST_NAME_1 = "John";
        static final String LAST_NAME_1 = "Doe";

        static final UUID ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        static final String EMAIL_2 = "JamesSmith@gmail.com";
        static final String HASHED_PASSWORD_2 = "{bcrypt}$2a$10$UAAKL7SVwwBjv9yDhXt1WOadQRM5XvSw7ls9YBbamrpF0GPpVjrda";
        static final String FIRST_NAME_2 = "James";
        static final String LAST_NAME_2 = "Smith";

        static final long ACCESS_TOKEN_EXP_MS = 3600000;
        static final long REFRESH_TOKEN_EXP_MS = 604800000;

        static final Instant NOW = Instant.now();
        static final Instant ACCESS_TOKEN_EXP = NOW.plusMillis(ACCESS_TOKEN_EXP_MS);
        static final Instant REFRESH_TOKEN_EXP = NOW.plusMillis(REFRESH_TOKEN_EXP_MS);

        static User buildFirstUser() {
            return User.builder()
                    .id(ID_1)
                    .email(EMAIL_1)
                    .password(HASHED_PASSWORD_1)
                    .firstName(FIRST_NAME_1)
                    .lastName(LAST_NAME_1)
                    .build();
        }

        static User buildSecondUser() {
            return User.builder()
                    .id(ID_2)
                    .email(EMAIL_2)
                    .password(HASHED_PASSWORD_2)
                    .firstName(FIRST_NAME_2)
                    .lastName(LAST_NAME_2)
                    .build();
        }
    }
}