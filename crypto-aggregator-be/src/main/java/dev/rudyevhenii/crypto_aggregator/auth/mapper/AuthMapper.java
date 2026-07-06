package dev.rudyevhenii.crypto_aggregator.auth.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.LoginRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.RefreshTokenRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.RegisterRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.TokenResponseRqDto;
import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
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
}
