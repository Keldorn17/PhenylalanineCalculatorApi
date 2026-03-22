package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.FoodTypeService;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(FoodTypeController.class)
@AutoConfigureRestTestClient
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
        Long id = TestEntityFactory.DEFAULT_ID;
        when(foodTypeService.findById(id)).thenThrow(FoodTypeNotFoundException.class);
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
    void findById_shouldReturn404_whenFoodTypeNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        when(foodTypeService.findById(id)).thenThrow(FoodTypeNotFoundException.class);
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
        Page<FoodTypeResponse> pageResponse = new PageImpl<>(List.of(TestEntityFactory.foodTypeResponse()));
        when(foodTypeService.findAll(anyInt(), anyInt())).thenReturn(pageResponse);
        TestPage<FoodTypeResponse> response = restTestClient.get()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodTypeResponse>>() {})
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        doAssertionsCheckOnResponse(response.content().getFirst(), pageResponse.getContent().getFirst());
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
    void putFoodType_shouldReturn404_whenFoodTypeNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodTypeRequest request =
                new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, TestEntityFactory.DEFAULT_INTEGER_VALUE);
        when(foodTypeService.update(id, request)).thenThrow(FoodTypeNotFoundException.class);
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
    void deleteById_shouldReturn404_whenFoodTypeNotFound() {
        Long id = TestEntityFactory.DEFAULT_ID;
        doThrow(FoodTypeNotFoundException.class).when(foodTypeService).deleteById(id);
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
