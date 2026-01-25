package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ForbiddenApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

    @Operation(
            summary = "Retrieves a food entry by id",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = FoodResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getById(@PathVariable Long id) {
        log.info("Get Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.findById(id));
    }

    @Operation(
            summary = "Retrieves all food entries",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = FoodResponse.class))
                            )
                    )
            }
    )
    @ForbiddenApiResponse
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAll() {
        log.info("Get All Request: {}", ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.findAll());
    }

    @Operation(
            summary = "Creates a food entry",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.CREATED,
                            description = SwaggerDescriptions.SUCCESS_CREATE,
                            content = @Content(schema = @Schema(implementation = FoodResponse.class))
                    )
            }
    )
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @PostMapping
    public ResponseEntity<FoodResponse> postFood(@Valid @RequestBody FoodRequest request) {
        log.info("Post Request: {}", ApiRoutes.FOOD_PATH);
        FoodResponse response = foodService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
            summary = "Updates a food entry by id",
            description = "All fields are optional. Only non-null fields will be applied to update.",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = FoodResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @PatchMapping("/{id}")
    public ResponseEntity<FoodResponse> patchFood(@PathVariable Long id, @RequestBody FoodUpdateRequest request) {
        log.info("Patch Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.update(id, request));
    }

    @Operation(
            summary = "Deletes a food entry by id",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        foodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
