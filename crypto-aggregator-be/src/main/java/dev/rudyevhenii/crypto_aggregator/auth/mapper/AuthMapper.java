package dev.rudyevhenii.crypto_aggregator.auth.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.auth.LoginRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.auth.LogoutRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.auth.RefreshTokenRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.auth.RegisterRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.auth.TokenResponseRqDto;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LogoutRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RefreshTokenRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RegisterRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    RegisterRequest map(RegisterRequestRqDto requestDto);

    TokenResponseRqDto map(TokenResponseDto responseDto);

    LoginRequest map(LoginRequestRqDto requestDto);

    RefreshTokenRequest map(RefreshTokenRequestRqDto requestDto);

    LogoutRequest map(LogoutRequestRqDto requestDto);
}
