package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ConflictApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.UnauthorizedApiResponse;
import com.keldorn.phenylalaninecalculatorapi.config.JwtProperties;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiPaths;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponseInternal;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.service.AuthService;
import com.keldorn.phenylalaninecalculatorapi.utils.HeaderUtils;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping(ApiRoutes.AUTH_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoint for authenticating, registering, refresh, password and username" +
        " change.")
public class AuthController {

    private final AuthService authService;
    private final HeaderUtils headerUtils;
    private final JwtProperties jwtProperties;

    private Long getExpiresIn() {
        return jwtProperties.getAccess().getExpirationTime().getSeconds();
    }

    @Operation(
            summary = "Authenticates the user, returning an access token and setting a refresh token cookie.",
            responses = {
                    @ApiResponse(responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @PostMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        log.info("Authenticate POST {}", ApiRoutes.AUTH_PATH);
        AuthResponseInternal response = authService.authenticate(request);
        return ResponseEntity.ok()
                .headers(headerUtils.getRefreshHeader(response.refreshToken()))
                .body(new AuthResponse(response.accessToken(), getExpiresIn()));
    }

    @Operation(
            summary = "Registers a new user, returning an access token and setting a refresh token cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @ConflictApiResponse
    @BadRequestApiResponse
    @PostMapping(ApiPaths.REGISTER)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        log.info("Register POST {}", ApiRoutes.AUTH_PATH);
        AuthResponseInternal response = authService.register(request);
        return ResponseEntity.ok()
                .headers(headerUtils.getRefreshHeader(response.refreshToken()))
                .body(new AuthResponse(response.accessToken(), getExpiresIn()));
    }

    @Operation(
            summary = "Refreshes the access token using the refresh token stored in the cookie, and updates the " +
                    "refresh token cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the updated HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @UnauthorizedApiResponse
    @PostMapping(ApiPaths.REFRESH)
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("Refresh POST {}", ApiRoutes.AUTH_PATH);
        AuthResponseInternal response = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .headers(headerUtils.getRefreshHeader(response.refreshToken()))
                .body(new AuthResponse(response.accessToken(), getExpiresIn()));
    }

    @Operation(
            summary = "Changes user's password, returning a new access token and updating the refresh token cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the updated HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @ConflictApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @PutMapping(ApiPaths.PASSWORD)
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody AuthPasswordChangeRequest request) {
        log.info("Password Change Request {}", ApiRoutes.AUTH_PATH);
        AuthResponseInternal response = authService.changePassword(request);
        return ResponseEntity.ok()
                .headers(headerUtils.getRefreshHeader(response.refreshToken()))
                .body(new AuthResponse(response.accessToken(), getExpiresIn()));
    }

    @Operation(
            summary = "Changes user's username, returning a new access token and updating the refresh token cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the updated HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @ConflictApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @PutMapping(ApiPaths.USERNAME)
    public ResponseEntity<AuthResponse> changeUsername(@Valid @RequestBody AuthUsernameChangeRequest request) {
        log.info("Username Change Request {}", ApiRoutes.AUTH_PATH);
        AuthResponseInternal response = authService.changeUsername(request);
        return ResponseEntity.ok()
                .headers(headerUtils.getRefreshHeader(response.refreshToken()))
                .body(new AuthResponse(response.accessToken(), getExpiresIn()));
    }

    @Operation(
            summary = "Logouts the user using the refresh token stored in the cookie, and updates the refresh token " +
                    "cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = AuthResponse.class)),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Contains the updated HttpOnly refresh token cookie ('refreshToken')",
                                    schema = @Schema(type = "string")
                            )
                    )
            }
    )
    @UnauthorizedApiResponse
    @PostMapping(ApiPaths.LOGOUT)
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("Logout POST {}", ApiRoutes.AUTH_PATH);
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .headers(headerUtils.getCleanRefreshHeader())
                .build();
    }

}
