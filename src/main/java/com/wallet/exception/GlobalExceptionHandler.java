package com.wallet.exception;

import com.wallet.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletFrozenException.class)
    public ResponseEntity<ErrorResponse> handleWalletFrozen(WalletFrozenException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "WALLET_FROZEN", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(InvalidTransactionException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_TRANSACTION", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIdempotencyKey(DuplicateIdempotencyKeyException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_IDEMPOTENCY_KEY", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedTransactionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedTransaction(UnauthorizedTransactionException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_TRANSACTION", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "CONCURRENT_UPDATE_CONFLICT",
                "Transaction failed due to concurrent wallet balance update. Please retry the request.", request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password", request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request payload validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
