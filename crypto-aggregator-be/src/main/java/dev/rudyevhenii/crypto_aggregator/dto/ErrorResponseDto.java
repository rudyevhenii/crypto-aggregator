package dev.rudyevhenii.crypto_aggregator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
public record ErrorResponseDto(
        int code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldError> errors
) {
    public record FieldError(String field, String error) {

    }
}
