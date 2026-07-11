package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RefreshTokenRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RegisterRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;
import dev.rudyevhenii.crypto_aggregator.auth.repository.UserRepository;
import dev.rudyevhenii.crypto_aggregator.auth.security.SecurityUserDetails;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GeneratorUtils generator;
    private final TokenBlacklistServiceImpl tokenBlacklistService;

    @Override
    public TokenResponseDto register(RegisterRequest request) {
        validateUserExists(request.email());
        User user = userRepository.create(toDomain(request));

        return generateTokens(user);
    }

    @Override
    public TokenResponseDto login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(), request.password()));

        SecurityUserDetails userDetails = (SecurityUserDetails) auth.getPrincipal();

        return generateTokens(userDetails.getUser());
    }

    @Override
    public TokenResponseDto refreshToken(RefreshTokenRequest refreshToken) {
        String email = jwtService.extractSubject(refreshToken.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (!jwtService.isTokenValid(refreshToken.refreshToken(), user)) {
            throw new JwtTokenExpirationException("Token is invalid for this user");
        }
        log.info("Generating refresh token for user {}", user.getId());
        return generateTokens(user);
    }

    @Override
    public void logout(String token) {
        log.info("Invalidating refresh token for user {}", token);
        Date expiration = jwtService.extractExpiration(token);
        Duration ttl = Duration.between(Instant.now(), expiration.toInstant());
        tokenBlacklistService.blacklist(token, ttl);
    }

    private User toDomain(RegisterRequest request) {
        return User.builder()
                .id(generator.uuid())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();
    }

    private TokenResponseDto generateTokens(User user) {
        return TokenResponseDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private void validateUserExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User with email '%s' already exists".formatted(email));
        }
    }
}
