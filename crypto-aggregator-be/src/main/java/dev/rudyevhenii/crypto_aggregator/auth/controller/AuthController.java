package dev.rudyevhenii.crypto_aggregator.auth.controller;

import dev.rudyevhenii.crypto_aggregator.api.dto.LoginRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.RefreshTokenRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.RegisterRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TokenResponseRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.AuthApi;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;
import dev.rudyevhenii.crypto_aggregator.auth.mapper.AuthMapper;
import dev.rudyevhenii.crypto_aggregator.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final AuthMapper mapper;

    @Override
    public ResponseEntity<TokenResponseRqDto> register(RegisterRequestRqDto request) {
        TokenResponseDto response = authService.register(mapper.toDto(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(response));
    }

    @Override
    public ResponseEntity<TokenResponseRqDto> login(LoginRequestRqDto request) {
        TokenResponseDto response = authService.login(mapper.toDto(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(mapper.toResponse(response));
    }

    @Override
    public ResponseEntity<TokenResponseRqDto> refreshToken(RefreshTokenRequestRqDto request) {
        TokenResponseDto response = authService.refreshToken(mapper.toDto(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(mapper.toResponse(response));
    }
}
