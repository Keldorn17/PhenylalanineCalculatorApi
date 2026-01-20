package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.service.FoodConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final String NOT_FOUND = "Not Found";
    private final String CONFLICT = "Conflict";
    private final String FORBIDDEN = "Forbidden";

    @Operation(operationId = "getAllFoodConsumptionByDate",
            summary = "Gets user's all food consumption for a specific day",
            tags = {"Food Consumption"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully found all food consumption by date", content = @Content(schema = @Schema(implementation = FoodConsumptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<FoodConsumptionResponse>> getAllFoodConsumptionByDate(@RequestParam LocalDate date) {
        log.info("Get request for getting all food consumption by date: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        return ResponseEntity.ok(foodConsumptionService.findAllByDate(date));
    }

    @Operation(operationId = "postFoodConsumption",
            summary = "Creates a field of food consumption for the user",
            tags = {"Food Consumption"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created food consumption", content = @Content(schema = @Schema(implementation = FoodConsumptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/{foodId}")
    public ResponseEntity<FoodConsumptionResponse> postFoodConsumption(@PathVariable Long foodId,
                                                                       @RequestBody FoodConsumptionRequest request) {
        log.info("Post request for: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        FoodConsumptionResponse response = foodConsumptionService.create(foodId, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(operationId = "putFoodConsumption",
            summary = "Updates a field of food consumption for the user by id",
            tags = {"Food Consumption"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated food consumption", content = @Content(schema = @Schema(implementation = FoodConsumptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<FoodConsumptionResponse> putFoodConsumption(@PathVariable Long id,
                                                                      @RequestBody FoodConsumptionRequest request) {
        log.info("Put request for: {}", ApiRoutes.FOOD_CONSUMPTION_PATH);
        return ResponseEntity.ok(foodConsumptionService.update(id, request));
    }

    @Operation(operationId = "deleteById",
            summary = "Deletes a field of food consumption for the user by id",
            tags = {"Food Consumption"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted food consumption"),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete request for id: {}, {}", id, ApiRoutes.FOOD_CONSUMPTION_PATH);
        foodConsumptionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
