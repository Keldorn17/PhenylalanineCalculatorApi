package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.service.FoodService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiRoutes.FOOD_PATH)
@Tag(name = "Food", description = "Endpoint for storing foods.")
public class FoodController {

    private final FoodService foodService;

    @Operation(operationId = "findById",
            summary = "Finds a food by its id.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "200", description = SwaggerDescriptions.SUCCESS_GET, content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "404", description = SwaggerDescriptions.NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = SwaggerDescriptions.FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getById(@PathVariable Long id) {
        log.info("Get Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.findById(id));
    }

    @Operation(operationId = "findAll",
            summary = "Finds all foods.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "200", description = SwaggerDescriptions.SUCCESS_GET, content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "403", description = SwaggerDescriptions.FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAll() {
        log.info("Get All Request: {}", ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.findAll());
    }

    @Operation(operationId = "postFood",
            summary = "Creates a new food.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "201", description = SwaggerDescriptions.SUCCESS_CREATE, content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "400", description = SwaggerDescriptions.BAD_REQUEST, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = SwaggerDescriptions.NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = SwaggerDescriptions.FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<FoodResponse> postFood(@Valid @RequestBody FoodRequest request) {
        log.info("Post Request: {}", ApiRoutes.FOOD_PATH);
        FoodResponse response = foodService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(operationId = "patchFood",
            summary = "Updates food by its id. All fields are optional. Only non-null fields will be applied to update.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "200", description = SwaggerDescriptions.SUCCESS_UPDATE, content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "404", description = SwaggerDescriptions.NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = SwaggerDescriptions.FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<FoodResponse> patchFood(@PathVariable Long id, @RequestBody FoodUpdateRequest request) {
        log.info("Patch Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.update(id, request));
    }

    @Operation(operationId = "deleteById",
            summary = "Deletes food by its id. Warning this is permanent.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "204", description = SwaggerDescriptions.SUCCESS_DELETE),
                    @ApiResponse(responseCode = "404", description = SwaggerDescriptions.NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = SwaggerDescriptions.FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        foodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
