package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RefreshTokenRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RegisterRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceAlreadyExistsException;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.user.domain.User;
import dev.rudyevhenii.crypto_aggregator.user.repository.DefaultUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DefaultUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GeneratorUtils generator;

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

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        return generateTokens(userDetails);
    }

    @Override
    public TokenResponseDto refreshToken(RefreshTokenRequest refreshToken) {
        UUID userId = jwtService.extractSubject(refreshToken.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (!jwtService.isTokenValid(refreshToken.refreshToken(), user)) {
            throw new JwtTokenExpirationException("Token is invalid for this user");
        }
        return generateTokens(user);
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

    private TokenResponseDto generateTokens(UserDetails userDetails) {
        return TokenResponseDto.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .refreshToken(jwtService.generateRefreshToken(userDetails))
                .build();
    }

    private void validateUserExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User already exists with email: %s".formatted(email));
        }
    }
}
