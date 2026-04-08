package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ConflictApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.UnauthorizedApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.service.FoodConsumptionService;

import java.net.URI;
import java.time.LocalDate;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
                            description = SwaggerDescriptions.SUCCESS_GET
                    )
            }
    )
    @GetMapping
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    public ResponseEntity<PagedModel<FoodConsumptionResponse>> getAllFoodConsumptionByDate(
            @Parameter(description = "Date of consumption (ISO-8601)", example = "2026-01-01")
            @RequestParam LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(required = false) String timezone
    ) {
        log.info("Get request for getting all food consumption by date: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        var result = foodConsumptionService.findAllByDate(date, page, size, timezone);
        return ResponseEntity.ok(new PagedModel<>(result));
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
    @ConflictApiResponse
    @NotFoundApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @PostMapping("/{foodId}")
    public ResponseEntity<FoodConsumptionResponse> postFoodConsumption(
            @Parameter(description = "ID of the food being consumed", example = "42")
            @PathVariable Long foodId,
            @Valid @RequestBody FoodConsumptionRequest request
    ) {
        log.info("Post request for: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        FoodConsumptionResponse response = foodConsumptionService.save(foodId, request);
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
    @NotFoundApiResponse
    @ConflictApiResponse
    @PutMapping("/{id}")
    @UnauthorizedApiResponse
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
    @NotFoundApiResponse
    @ConflictApiResponse
    @UnauthorizedApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete request for id: {}, {}", id, ApiRoutes.FOOD_CONSUMPTION_PATH);
        foodConsumptionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
