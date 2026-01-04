package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.service.FoodTypeService;
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
@RequestMapping(ApiRoutes.FOOD_TYPE_PATH)
@RequiredArgsConstructor
@Tag(name = "Food Type", description = "Endpoint for storing food types and its multiplier")
public class FoodTypeController {

    private final FoodTypeService foodTypeService;

    private final String NOT_FOUND = "Food Type Not Found";
    private final String UNAUTHORIZED = "Unauthorized";
    private final String BAD_REQUEST = "Bad Request";

    @Operation(operationId = "findById",
            summary = "Finds a food type by its id",
            tags = {"Food Type"},
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully found food type", content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))),
                @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = UNAUTHORIZED, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<FoodTypeResponse> findById(@PathVariable Long id) {
        log.info("Get Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        return ResponseEntity.ok(foodTypeService.findById(id));
    }

    @Operation(operationId = "findAll",
            summary = "Finds all food type.",
            tags = {"Food Type"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully found all food type", content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))),
                    @ApiResponse(responseCode = "403", description = UNAUTHORIZED, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<FoodTypeResponse>> findAll() {
        log.info("Get All: {}", ApiRoutes.FOOD_TYPE_PATH);
        return ResponseEntity.ok(foodTypeService.findAll());
    }

    @Operation(operationId = "postFoodType",
            summary = "Creates a new food type",
            tags = {"Food Type"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created food type", content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = UNAUTHORIZED, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<FoodTypeResponse> postFoodType(@Valid @RequestBody FoodTypeRequest foodTypeRequest) {
        log.info("Post Request: {}", ApiRoutes.FOOD_TYPE_PATH);
        var response = foodTypeService.save(foodTypeRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(operationId = "putFoodType",
            summary = "Updates food type by id",
            tags = {"Food Type"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated food type", content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))),
                    @ApiResponse(responseCode = "400", description = BAD_REQUEST, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = UNAUTHORIZED, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<FoodTypeResponse> putFoodType(@PathVariable Long id, @Valid @RequestBody FoodTypeRequest foodTypeRequest) {
        log.info("Put Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        return ResponseEntity.ok(foodTypeService.update(id, foodTypeRequest));
    }

    @Operation(operationId = "deleteById",
            summary = "Deletes food type by its id",
            tags = {"Food Type"},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted food type", content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = UNAUTHORIZED, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        foodTypeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
