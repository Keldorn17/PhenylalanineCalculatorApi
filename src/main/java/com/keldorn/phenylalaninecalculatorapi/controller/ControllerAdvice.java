package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    private final String CLIENT_ERROR = "Client Error";
    private final String INTERNAL_ERROR = "Internal Error";

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        return buildAndLog(HttpStatus.NOT_FOUND, CLIENT_ERROR, ex);
    }

    @ExceptionHandler({EmailIsTakenException.class, UsernameIsTakenException.class})
    public ResponseEntity<Object> handleConflict(Exception ex) {
        return buildAndLog(HttpStatus.CONFLICT, CLIENT_ERROR, ex);
    }

    @ExceptionHandler(InvalidJwtTokenReceivedException.class)
    public ResponseEntity<Object> handleUnauthorized(Exception ex) {
        return buildAndLog(HttpStatus.UNAUTHORIZED, CLIENT_ERROR, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalError(Exception ex) {
        return buildAndLog(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR, ex);
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
