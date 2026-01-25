package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ConflictApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ForbiddenApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.service.FoodConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiRoutes.FOOD_CONSUMPTION_PATH)
@Tag(name = "Food Consumption", description = "Endpoint for storing the user's food consumption")
public class FoodConsumptionController {

    private final FoodConsumptionService foodConsumptionService;

    @Operation(
            summary = "Retrieves all food consumption entries by date",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = FoodConsumptionResponse.class))
                            )
                    )
            }
    )
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @GetMapping
    public ResponseEntity<List<FoodConsumptionResponse>> getAllFoodConsumptionByDate(
            @Parameter(description = "Date of consumption (ISO-8601)", example = "2026-01-01")
            @RequestParam LocalDate date
    ) {
        log.info("Get request for getting all food consumption by date: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        return ResponseEntity.ok(foodConsumptionService.findAllByDate(date));
    }

    @Operation(
            summary = "Creates a food consumption entry for user",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.CREATED,
                            description = SwaggerDescriptions.SUCCESS_CREATE,
                            content = @Content(schema = @Schema(implementation = FoodConsumptionResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @PostMapping("/{foodId}")
    public ResponseEntity<FoodConsumptionResponse> postFoodConsumption(
            @Parameter(description = "ID of the food being consumed", example = "42")
            @PathVariable Long foodId,
            @Valid @RequestBody FoodConsumptionRequest request
    ) {
        log.info("Post request for: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        FoodConsumptionResponse response = foodConsumptionService.create(foodId, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
            summary = "Updates a food consumption entry by id",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = FoodConsumptionResponse.class))
                    )
            }
    )
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @PutMapping("/{id}")
    public ResponseEntity<FoodConsumptionResponse> putFoodConsumption(@PathVariable Long id,
                                                                      @Valid @RequestBody FoodConsumptionRequest request) {
        log.info("Put request for: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        return ResponseEntity.ok(foodConsumptionService.update(id, request));
    }

    @Operation(
            summary = "Deletes a food consumption entry by id",
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
    @ConflictApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete request for id: {}, {}", id, ApiRoutes.FOOD_CONSUMPTION_PATH);
        foodConsumptionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
