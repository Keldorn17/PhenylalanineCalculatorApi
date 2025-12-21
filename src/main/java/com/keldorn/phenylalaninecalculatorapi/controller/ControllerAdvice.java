package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
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
    public ResponseEntity<Object> handleException(UsernameNotFoundException exception) {
        log.warn("UsernameNotFoundException {}", exception.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(CLIENT_ERROR)
                .title(status.getReasonPhrase())
                .statusCode(status)
                .details(exception.getMessage())
                .build();
        return builderResponse(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) {
        log.warn("Exception {}", exception.getMessage());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(INTERNAL_ERROR)
                .title(status.getReasonPhrase())
                .statusCode(status)
                .details(exception.getMessage())
                .build();
        return builderResponse(errorResponse);
    }

    private ResponseEntity<Object> builderResponse(ErrorResponse errorResponse) {
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
    }
}
