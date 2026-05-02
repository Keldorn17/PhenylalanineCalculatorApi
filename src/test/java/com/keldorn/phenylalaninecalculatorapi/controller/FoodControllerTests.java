package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.PagedFoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.page.PageResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.FoodService;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@WebMvcTest(FoodController.class)
public class FoodControllerTests {

    @MockitoBean
    private FoodService foodService;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void getById_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodResponse expectedResponse = TestEntityFactory.foodResponse();
        when(foodService.findById(id)).thenReturn(expectedResponse);
        FoodResponse response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsChecksOnResponse(response, expectedResponse);
    }

    @Test
    void getById_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        when(foodService.findById(id)).thenThrow(ResourceNotFoundException.class);
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAll_shouldReturn200() {
        PagedFoodResponse pagedFoodResponse =
                new PagedFoodResponse(List.of(TestEntityFactory.foodResponse()), new PageResponse());
        Page<FoodResponse> pageResponse = new PageImpl<>(List.of(TestEntityFactory.foodResponse()));
        when(foodService.findAll(any(QueryRequest.class), any(PaginationRequest.class))).thenReturn(pagedFoodResponse);
        PagedFoodResponse response = restTestClient.get()
                .uri(ApiRoutes.FOOD_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedFoodResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        doAssertionsChecksOnResponse(response.getContent().getFirst(), pageResponse.getContent().getFirst());
    }

    @Test
    void postFood_shouldReturn201() {
        FoodRequest request = new FoodRequest(
                TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID
        );
        FoodResponse expectedResponse = TestEntityFactory.foodResponse();
        when(foodService.save(request)).thenReturn(expectedResponse);
        FoodResponse response = restTestClient.post()
                .uri(ApiRoutes.FOOD_PATH)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FoodResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsChecksOnResponse(response, expectedResponse);
    }

    @Test
    void postFood_shouldReturn400_whenMissingRequiredInputIsPresent() {
        FoodRequest request = new FoodRequest(
                TestEntityFactory.DEFAULT_FOOD_NAME,
                null,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID
        );
        restTestClient.post()
                .uri(ApiRoutes.FOOD_PATH)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void postFood_shouldReturn404_whenResourceNotFound() {
        FoodRequest request = new FoodRequest(
                TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID
        );
        when(foodService.save(request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.post()
                .uri(ApiRoutes.FOOD_PATH)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchFood_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodUpdateRequest request = new FoodUpdateRequest(
                TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID
        );
        FoodResponse expectedResponse = TestEntityFactory.foodResponse();
        when(foodService.update(id, request)).thenReturn(expectedResponse);
        FoodResponse response = restTestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsChecksOnResponse(response, expectedResponse);
    }

    @Test
    void patchFood_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodUpdateRequest request = new FoodUpdateRequest(
                TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID
        );
        when(foodService.update(id, request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_shouldReturn204() {
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteById_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        doThrow(ResourceNotFoundException.class).when(foodService).deleteById(id);
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound();
    }

    private void doAssertionsChecksOnResponse(FoodResponse response, FoodResponse expectedResponse) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.name()).isEqualTo(expectedResponse.name());
        Assertions.assertThat(response.multiplier()).isEqualTo(expectedResponse.multiplier());
        Assertions.assertThat(response.foodTypeName()).isEqualTo(expectedResponse.foodTypeName());
        Assertions.assertThat(response.protein()).isEqualByComparingTo(expectedResponse.protein());
        Assertions.assertThat(response.calories()).isEqualByComparingTo(expectedResponse.calories());
        Assertions.assertThat(response.phenylalanine()).isEqualByComparingTo(expectedResponse.phenylalanine());
    }

}
