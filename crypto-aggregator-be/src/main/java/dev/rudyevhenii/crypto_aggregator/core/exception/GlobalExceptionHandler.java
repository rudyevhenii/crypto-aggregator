package dev.rudyevhenii.crypto_aggregator.core.exception;

import dev.rudyevhenii.crypto_aggregator.core.dto.ErrorResponseDto;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.tomcat.websocket.AuthenticationException;
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
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logErrorMessage(status, ex);

        BindingResult result = ex.getBindingResult();
        List<ErrorResponseDto.FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(err -> new ErrorResponseDto.FieldError(err.getField(), err.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage(), fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ConstraintViolationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' value '%s' is invalid", ex.getName(), ex.getValue());
        log.warn("Type mismatch: {}", message);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, message));
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleException(InvalidJwtTokenException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(JwtTokenExpirationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(JwtTokenExpirationException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDto> handleException(JwtException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ResourceAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleException(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleException(AuthenticationException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDto> handleException(WebClientResponseException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        log.warn("External Exchange API failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getResponseBodyAsString()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleException(UnauthorizedException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logErrorMessage(status, ex);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, ex.getMessage()));
    }

    private void logErrorMessage(HttpStatus status, Exception ex) {
        log.warn("Status: {}, message: {}", status, ex.getMessage());
    }

    private ErrorResponseDto buildErrorResponse(HttpStatus status, String message) {
        return ErrorResponseDto.builder()
                .code(status.value())
                .message(message)
                .build();
    }

    private ErrorResponseDto buildErrorResponse(HttpStatus status, String message,
                                                List<ErrorResponseDto.FieldError> fieldErrors) {
        return ErrorResponseDto.builder()
                .code(status.value())
                .message(message)
                .errors(fieldErrors)
                .build();
    }
}
