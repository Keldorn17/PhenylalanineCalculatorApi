package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.UnauthorizedApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.service.FoodTypeService;

import java.net.URI;

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
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping(ApiRoutes.FOOD_TYPE_PATH)
@RequiredArgsConstructor
@Tag(name = "Food Type", description = "Endpoint for storing food types and its multiplier")
public class FoodTypeController {

    private final FoodTypeService foodTypeService;

    @Operation(
            summary = "Retrieves a food type entry by id",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))
                    )
            }
    )
    @NotFoundApiResponse
    @BadRequestApiResponse
    @GetMapping("/{id}")
    @UnauthorizedApiResponse
    public ResponseEntity<FoodTypeResponse> findById(@PathVariable Long id) {
        log.info("Get Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        return ResponseEntity.ok(foodTypeService.findById(id));
    }

    @Operation(
            summary = "Retrieves all food type entries",
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
    public ResponseEntity<PagedModel<FoodTypeResponse>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        log.info("Get All: {}", ApiRoutes.FOOD_TYPE_PATH);
        var result = foodTypeService.findAll(page, size);
        return ResponseEntity.ok(new PagedModel<>(result));
    }

    @Operation(
            summary = "Creates a food type entry",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.CREATED,
                            description = SwaggerDescriptions.SUCCESS_CREATE,
                            content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))
                    )
            }
    )
    @PostMapping
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    public ResponseEntity<FoodTypeResponse> postFoodType(@Valid @RequestBody FoodTypeRequest foodTypeRequest) {
        log.info("Post Request: {}", ApiRoutes.FOOD_TYPE_PATH);
        var response = foodTypeService.save(foodTypeRequest);
        URI uri = UriComponentsBuilder.fromUriString(ApiRoutes.FOOD_TYPE_PATH_BY_ID)
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
            summary = "Updates a food type entry by id",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_UPDATE,
                            content = @Content(schema = @Schema(implementation = FoodTypeResponse.class))
                    )
            }
    )
    @NotFoundApiResponse
    @BadRequestApiResponse
    @PutMapping("/{id}")
    @UnauthorizedApiResponse
    public ResponseEntity<FoodTypeResponse> putFoodType(@PathVariable Long id,
            @Valid @RequestBody FoodTypeRequest foodTypeRequest) {
        log.info("Put Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        return ResponseEntity.ok(foodTypeService.update(id, foodTypeRequest));
    }

    @Operation(
            summary = "Deletes a food type entry by id",
            description = "Warning this is permanent",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.NO_CONTENT,
                            description = SwaggerDescriptions.SUCCESS_DELETE
                    )
            }
    )
    @NotFoundApiResponse
    @UnauthorizedApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {}: {}", id, ApiRoutes.FOOD_TYPE_PATH);
        foodTypeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
