package dev.rudyevhenii.crypto_aggregator.exception;

import dev.rudyevhenii.crypto_aggregator.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AsyncRequestNotUsableException.class, ClientAbortException.class})
    public void handleAsyncRequestNotUsableException() {
    }

    @ExceptionHandler(UnsupportedIntervalException.class)
    public ResponseEntity<ErrorResponseDto> handleException(UnsupportedIntervalException ex) {
        log.warn("Business validation failed: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MethodArgumentNotValidException ex) {
        log.warn("Input validation failed");
        HttpStatus status = HttpStatus.BAD_REQUEST;

        BindingResult result = ex.getBindingResult();
        List<ErrorResponseDto.FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(err -> new ErrorResponseDto.FieldError(err.getField(), err.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), ex.getMessage(), fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' value '%s' is invalid", ex.getName(), ex.getValue());
        log.warn("Type mismatch: {}", message);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), message));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDto> handleException(WebClientResponseException ex) {
        log.error("External Exchange API failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), ex.getResponseBodyAsString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        log.error("Unknown internal server error occurred", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status.value(), ex.getMessage()));
    }

    private ErrorResponseDto buildErrorResponse(int code, String message) {
        return ErrorResponseDto.builder()
                .code(code)
                .message(message)
                .build();
    }

    private ErrorResponseDto buildErrorResponse(int code, String message, List<ErrorResponseDto.FieldError> fieldErrors) {
        return ErrorResponseDto.builder()
                .code(code)
                .message(message)
                .errors(fieldErrors)
                .build();
    }
}
