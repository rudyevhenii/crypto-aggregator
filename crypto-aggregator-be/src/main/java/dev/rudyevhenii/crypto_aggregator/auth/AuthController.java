package dev.rudyevhenii.crypto_aggregator.auth;

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
import org.springframework.web.context.request.NativeWebRequest;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final AuthMapper mapper;
    private final NativeWebRequest webRequest;

    @Override
    public ResponseEntity<TokenResponseRqDto> register(RegisterRequestRqDto request) {
        TokenResponseDto response = authService.register(mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.map(response));
    }

    @Override
    public ResponseEntity<TokenResponseRqDto> login(LoginRequestRqDto request) {
        TokenResponseDto response = authService.login(mapper.map(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(mapper.map(response));
    }

    @Override
    public ResponseEntity<TokenResponseRqDto> refreshToken(RefreshTokenRequestRqDto request) {
        TokenResponseDto response = authService.refreshToken(mapper.map(request));
        return ResponseEntity.status(HttpStatus.OK)
                .body(mapper.map(response));
    }

    @Override
    public ResponseEntity<Void> logout() {
        String authHeader = webRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent()
                .build();
    }
}
