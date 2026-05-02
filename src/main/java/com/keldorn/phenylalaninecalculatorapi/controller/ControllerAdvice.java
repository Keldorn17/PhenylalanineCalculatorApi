package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidRSQLException;
import com.keldorn.phenylalaninecalculatorapi.exception.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.DeletedUserTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        return buildAndLog(HttpStatus.NOT_FOUND, ApiResponses.CLIENT_ERROR, ex);
    }

    @ExceptionHandler({EmailIsTakenException.class, UsernameIsTakenException.class, PasswordMismatchException.class,
            DailyIntakeCannotBeLowerThanZeroException.class
    })
    public ResponseEntity<Object> handleConflict(Exception ex) {
        return buildAndLog(HttpStatus.CONFLICT, ApiResponses.CLIENT_ERROR, ex);
    }

    @ExceptionHandler({InvalidJwtTokenReceivedException.class, BadCredentialsException.class,
            DeletedUserTokenReceivedException.class})
    public ResponseEntity<Object> handleUnauthorized(Exception ex) {
        return buildAndLog(HttpStatus.UNAUTHORIZED, ApiResponses.CLIENT_ERROR, ex);
    }

    @ExceptionHandler(InvalidRSQLException.class)
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        return buildAndLog(HttpStatus.BAD_REQUEST, ApiResponses.CLIENT_ERROR, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalError(Exception ex) {
        return buildAndLog(HttpStatus.INTERNAL_SERVER_ERROR, ApiResponses.INTERNAL_ERROR, ex);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Malformed data received: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .type(ApiResponses.CLIENT_ERROR)
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST)
                .details(ApiResponses.MALFORMED_RESPONSE)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleException(MissingServletRequestParameterException ex) {
        log.error("Missing require parameter: {}", ex.getMessage());
        String missingParam = ex.getMessage().split("'")[1];
        ErrorResponse response = ErrorResponse.builder()
                .type(ApiResponses.CLIENT_ERROR)
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST)
                .details(ApiResponses.REQUIRED_MISSING_RESPONSE.formatted(missingParam))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", details);
        ErrorResponse response = ErrorResponse.builder()
                .type(ApiResponses.CLIENT_ERROR)
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST)
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ResponseEntity<Object> buildAndLog(HttpStatus status, String type, Exception ex) {
        boolean isError = status.is5xxServerError();
        if (isError) {
            log.error("Internal server error: ", ex);
        } else {
            log.warn("{} : {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
        ErrorResponse response = ErrorResponse.builder()
                .type(type)
                .title(status.getReasonPhrase())
                .statusCode(status)
                .details(getUserFriendlyMessage(ex, status))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String getUserFriendlyMessage(Exception ex, HttpStatus status) {
        if (status.is5xxServerError()) {
            return ApiResponses.INTERNAL_RESPONSE;
        }
        return switch (ex.getClass().getSimpleName()) {
            case "EmailIsTakenException" -> ApiResponses.EMAIL_IS_TAKEN_RESPONSE;
            case "UsernameIsTakenException" -> ApiResponses.USERNAME_IS_TAKEN_RESPONSE;
            case "PasswordMismatchException" -> ApiResponses.PASSWORD_MISMATCH_RESPONSE;
            case "DeletedUserTokenReceivedException" -> ApiResponses.DELETED_ACCOUNT_RESPONSE;
            case "InvalidJwtTokenReceivedException", "BadCredentialsException" -> ApiResponses.UNAUTHORIZED_RESPONSE;
            case "ResourceNotFoundException" -> ApiResponses.RESOURCE_NOT_FOUND_RESPONSE;
            case "DailyIntakeCannotBeLowerThanZeroException" -> ApiResponses.DAILY_INTAKE_NEGATIVE_RESPONSE;
            case "InvalidRSQLException" -> ApiResponses.INVALID_RSQL_RESPONSE;
            default -> ApiResponses.DEFAULT_RESPONSE;
        };
    }

}
