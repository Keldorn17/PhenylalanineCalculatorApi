package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.ConflictApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ForbiddenApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.*;
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

    @Operation(
            summary = "Authenticates the user and sends back a token.",
            responses = {
                    @ApiResponse(responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        log.info("Authenticate POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(
            summary = "Registers a new user and sends back a token.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        log.info("Register POST {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Changes user's password.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @PutMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody AuthPasswordChangeRequest request) {
        log.info("Password Change Request {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @Operation(
            summary = "Changes user's username.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @PutMapping("/username")
    public ResponseEntity<AuthResponse> changeUsername(@Valid @RequestBody AuthUsernameChangeRequest request) {
        log.info("Username Change Request {}", ApiRoutes.AUTH_PATH);
        return ResponseEntity.ok(authService.changeUsername(request));
    }
}
