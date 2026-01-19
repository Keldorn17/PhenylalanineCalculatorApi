package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
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

    private final String FORBIDDEN = "Forbidden";
    private final String USER_NOT_FOUND = "User Not Found";

    @Operation(operationId = "me",
            summary = "Gets the current users information.",
            tags = {"User"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<UserResponse> me() {
        log.info("Get Request: {}", ApiRoutes.USER_PATH);
        return ResponseEntity.ok(userService.getProfile());
    }

    @Operation(operationId = "updateUser",
            summary = "Updates user information. All fields are optional. Only non-null fields will be applied to update.",
            tags = {"User"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Patch Request: {}", ApiRoutes.USER_PATH);
        return ResponseEntity.ok(userService.update(userRequest));
    }

    @Operation(operationId = "deleteUser",
            summary = "Deletes user. Warning this is permanent.",
            tags = {"User"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion"),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = USER_NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        log.info("Delete Request: {}", ApiRoutes.USER_PATH);
        userService.delete();
        return ResponseEntity.noContent().build();
    }
}
