package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.*;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiRoutes.AUTH_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoint for authenticating, registering, password and username change.")
public class AuthController {

    private final AuthService authService;

    private final String FORBIDDEN = "Forbidden";
    private final String USER_NOT_FOUND = "User Not Found";
    private final String CONFLICT = "Conflict";

    @Operation(operationId = "authenticate",
            summary = "Authenticates the user and sends back a token.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful authentication", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        log.info("Authenticate POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(operationId = "register",
            summary = "Registers a new user and sends back a token.",
            tags = {"Authentication" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful authentication", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        log.info("Register POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(operationId = "changePassword",
            summary = "Changes users password",
            tags = {"Authentication" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful password change", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody AuthPasswordChangeRequest request) {
        log.info("Password Change Request {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @Operation(operationId = "changeUsername",
            summary = "Changes users username",
            tags = {"Authentication" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful username change", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/username")
    public ResponseEntity<AuthResponse> changeUsername(@Valid @RequestBody AuthUsernameChangeRequest request) {
        log.info("Username Change Request {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.changeUsername(request));
    }
}
