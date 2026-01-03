package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiRoutes.AUTH_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoint for authenticating and registering.")
public class AuthController {

    private final AuthService authService;

    @Operation(operationId = "authenticate",
            summary = "Authenticates the user and sends back a token.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful authentication", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User Not Found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        log.info("Authenticate POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(operationId = "register",
            summary = "Registers a new user and sends back a token.",
            tags = {"Authentication" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful authentication", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User Not Found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRegisterRequest request) {
        log.info("Register POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.register(request));
    }
}
