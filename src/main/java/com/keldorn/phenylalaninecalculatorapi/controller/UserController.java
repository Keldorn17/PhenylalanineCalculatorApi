package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.ForbiddenApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.service.UserService;
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
@RequiredArgsConstructor
@RequestMapping(ApiRoutes.USER_PATH)
@Tag(name = "User", description = "Endpoint for basic user information retrieval and modification")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Retrieves the authenticated user's information.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @GetMapping
    public ResponseEntity<UserResponse> me() {
        log.info("Get Request: {}", ApiRoutes.USER_PATH);
        return ResponseEntity.ok(userService.getProfile());
    }

    @Operation(
            summary = "Updates user information",
            description = "All fields are optional. Only non-null fields will be applied to update.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Patch Request: {}", ApiRoutes.USER_PATH);
        return ResponseEntity.ok(userService.update(userRequest));
    }

    @Operation(
            summary = "Deletes the authenticated user",
            description = "Warning this is permanent",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.NO_CONTENT,
                            description = SwaggerDescriptions.SUCCESS_DELETE
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        log.info("Delete Request: {}", ApiRoutes.USER_PATH);
        userService.delete();
        return ResponseEntity.noContent().build();
    }
}
