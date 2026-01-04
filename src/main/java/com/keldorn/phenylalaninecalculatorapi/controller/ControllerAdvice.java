package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    private final String CLIENT_ERROR = "Client Error";
    private final String INTERNAL_ERROR = "Internal Error";

    @ExceptionHandler({UsernameNotFoundException.class, FoodTypeNotFoundException.class})
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        return buildAndLog(HttpStatus.NOT_FOUND, CLIENT_ERROR, ex);
    }

    @ExceptionHandler({EmailIsTakenException.class, UsernameIsTakenException.class})
    public ResponseEntity<Object> handleConflict(Exception ex) {
        return buildAndLog(HttpStatus.CONFLICT, CLIENT_ERROR, ex);
    }

    @ExceptionHandler({InvalidJwtTokenReceivedException.class, BadCredentialsException.class})
    public ResponseEntity<Object> handleUnauthorized(Exception ex) {
        return buildAndLog(HttpStatus.UNAUTHORIZED, CLIENT_ERROR, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalError(Exception ex) {
        return buildAndLog(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", details);

        ErrorResponse response = ErrorResponse.builder()
                .type(CLIENT_ERROR)
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
                .details(isError ? "An internal error occurred" : ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
