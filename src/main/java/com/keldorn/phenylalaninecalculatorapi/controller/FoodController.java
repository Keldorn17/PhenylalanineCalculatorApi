package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
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

    private final String NOT_FOUND = "Food Not Found";
    private final String NOT_FOUND_FOOD_TYPE = "Food Type Not Found";
    private final String FORBIDDEN = "Forbidden";
    private final String BAD_REQUEST = "Bad Request";

    @Operation(operationId = "findById",
            summary = "Finds a food by its id.",
            tags = {"Food"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully found food", content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    @ApiResponse(responseCode = "200", description = "Successfully found all foods", content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    @ApiResponse(responseCode = "201", description = "Successfully created food", content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND_FOOD_TYPE, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    @ApiResponse(responseCode = "200", description = "Successfully updated food", content = @Content(schema = @Schema(implementation = FoodResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                    @ApiResponse(responseCode = "200", description = "Successfully deleted food"),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        foodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
