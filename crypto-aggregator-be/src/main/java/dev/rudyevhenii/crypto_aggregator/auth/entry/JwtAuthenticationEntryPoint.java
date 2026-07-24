package dev.rudyevhenii.crypto_aggregator.auth.entry;

import dev.rudyevhenii.crypto_aggregator.core.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);

        String message = authException.getMessage();
        if (message == null || message.isBlank()) {
            message = "Unauthorized";
        }
        ErrorResponseDto errorResponse = buildErrorResponse(statusCode, message);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private ErrorResponseDto buildErrorResponse(int statusCode, String message) {
        return ErrorResponseDto.builder()
                .code(statusCode)
                .message(message)
                .build();
    }
}
