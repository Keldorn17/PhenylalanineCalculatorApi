package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.page.PageResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.FoodTypeService;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@WebMvcTest(FoodTypeController.class)
public class FoodTypeControllerTests {

    @MockitoBean
    private FoodTypeService foodTypeService;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void findById_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodTypeResponse expectedResponse = TestEntityFactory.foodTypeResponse();
        when(foodTypeService.findById(id)).thenReturn(expectedResponse);
        FoodTypeResponse response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodTypeResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void findById_shouldReturn400_whenIdIsMalformed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment("Invalid Id")
                        .build()
                )
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void findById_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        when(foodTypeService.findById(id)).thenThrow(ResourceNotFoundException.class);
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findAll_shouldReturn200() {
        PagedFoodTypeResponse pageResponse =
                new PagedFoodTypeResponse(List.of(TestEntityFactory.foodTypeResponse()), new PageResponse());
        when(foodTypeService.findAll(any(PaginationRequest.class))).thenReturn(pageResponse);
        PagedFoodTypeResponse response = restTestClient.get()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedFoodTypeResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        doAssertionsCheckOnResponse(response.getContent().getFirst(), pageResponse.getContent().getFirst());
    }

    @Test
    void postFoodType_shouldReturn201() {
        FoodTypeResponse expectedResponse = TestEntityFactory.foodTypeResponse();
        FoodTypeRequest request =
                new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, TestEntityFactory.DEFAULT_INTEGER_VALUE);
        when(foodTypeService.save(request)).thenReturn(expectedResponse);
        FoodTypeResponse response = restTestClient.post()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FoodTypeResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void postFoodType_shouldReturn400_whenRequiredDataIsMissing() {
        FoodTypeResponse expectedResponse = TestEntityFactory.foodTypeResponse();
        FoodTypeRequest request = new FoodTypeRequest(null, null);
        when(foodTypeService.save(request)).thenReturn(expectedResponse);
        restTestClient.post()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void putFoodType_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodTypeResponse expectedResponse = TestEntityFactory.foodTypeResponse();
        FoodTypeRequest request =
                new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, TestEntityFactory.DEFAULT_INTEGER_VALUE);
        when(foodTypeService.update(id, request)).thenReturn(expectedResponse);
        FoodTypeResponse response = restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodTypeResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void putFoodType_shouldReturn400_whenRequiredDataIsMissing() {
        FoodTypeRequest request = new FoodTypeRequest(null, null);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void putFoodType_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodTypeRequest request =
                new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, TestEntityFactory.DEFAULT_INTEGER_VALUE);
        when(foodTypeService.update(id, request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
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
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteById_shouldReturn404_whenResourceNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        doThrow(ResourceNotFoundException.class).when(foodTypeService).deleteById(id);
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_TYPE_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound();
    }

    private void doAssertionsCheckOnResponse(FoodTypeResponse response, FoodTypeResponse expectedResponse) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.name()).isEqualTo(expectedResponse.name());
        Assertions.assertThat(response.multiplier()).isEqualTo(expectedResponse.multiplier());
    }

}
