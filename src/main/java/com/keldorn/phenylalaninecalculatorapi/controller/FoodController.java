package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.UnauthorizedApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.PagedFoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;
import com.keldorn.phenylalaninecalculatorapi.service.FoodService;

import java.net.URI;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @NotFoundApiResponse
    @GetMapping("/{id}")
    @UnauthorizedApiResponse
    public ResponseEntity<FoodResponse> getById(@PathVariable Long id) {
        log.info("Get Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        return ResponseEntity.ok(foodService.findById(id));
    }

    @Operation(
            summary = "Retrieves all food entries",
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
    public ResponseEntity<PagedFoodResponse> getAll(@ParameterObject QueryRequest queryRequest, @ParameterObject
    PaginationRequest pageRequest) {
        log.info("Get All Request: {}", ApiRoutes.FOOD_PATH);
        var result = foodService.findAll(queryRequest, pageRequest);
        return ResponseEntity.ok(result);
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
    @PostMapping
    @NotFoundApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    public ResponseEntity<FoodResponse> postFood(@Valid @RequestBody FoodRequest request) {
        log.info("Post Request: {}", ApiRoutes.FOOD_PATH);
        FoodResponse response = foodService.save(request);
        URI uri = UriComponentsBuilder.fromUriString(ApiRoutes.FOOD_PATH_BY_ID)
                .buildAndExpand(response.id())
                .toUri();
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
    @NotFoundApiResponse
    @UnauthorizedApiResponse
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
    @NotFoundApiResponse
    @UnauthorizedApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Delete Request to {} : {}", id, ApiRoutes.FOOD_PATH);
        foodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
