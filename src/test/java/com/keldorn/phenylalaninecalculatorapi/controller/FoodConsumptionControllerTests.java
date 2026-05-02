package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.PagedFoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.page.PageResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.params.PaginationRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.FoodConsumptionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@WebMvcTest(FoodConsumptionController.class)
public class FoodConsumptionControllerTests {

    @MockitoBean
    private FoodConsumptionService foodConsumptionService;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void getAllFoodConsumptionByDate_shouldReturn200() {
        LocalDate testDate = TestEntityFactory.TEST_DATE;
        PaginationRequest paginationRequest = new PaginationRequest(0, 20);
        FoodConsumptionResponse expectedResponse = TestEntityFactory.foodConsumptionResponse();
        PagedFoodConsumptionResponse pageResponse =
                new PagedFoodConsumptionResponse(List.of(expectedResponse), new PageResponse());
        when(foodConsumptionService.findAllByDate(testDate, paginationRequest, TestEntityFactory.UTC_TIMEZONE)).thenReturn(
                pageResponse);
        PagedFoodConsumptionResponse response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", testDate)
                        .queryParam("page", paginationRequest.getPageNumber())
                        .queryParam("size", paginationRequest.getPageSize())
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedFoodConsumptionResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getContent()).hasSize(1);
        doAssertionsCheckOnResponse(response.getContent().getFirst(), expectedResponse);
    }

    @Test
    void getAllFoodConsumptionByDate_shouldReturn400_whenDateIsMalformed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", "invalid")
                        .build()
                )
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAllFoodConsumptionByDate_shouldReturn400_whenDateIsMissing() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .build()
                )
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void postFoodConsumption_shouldReturn201() {
        Long foodId = 42L;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);
        FoodConsumptionResponse expectedResponse = TestEntityFactory.foodConsumptionResponse();
        when(foodConsumptionService.save(foodId, request, TestEntityFactory.UTC_TIMEZONE)).thenReturn(expectedResponse);
        FoodConsumptionResponse response = restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(foodId))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FoodConsumptionResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void postFoodConsumption_shouldReturn400_whenBodyContentIsNull() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(null);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void postFoodConsumption_shouldReturn404_whenResourceNotFound() {
        when(foodConsumptionService.save(anyLong(), any(FoodConsumptionRequest.class), anyString())).thenThrow(
                ResourceNotFoundException.class);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postFoodConsumption_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        when(foodConsumptionService.save(anyLong(), any(FoodConsumptionRequest.class), anyString())).thenThrow(
                DailyIntakeCannotBeLowerThanZeroException.class);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void putFoodConsumption_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodConsumptionRequest request = new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
        FoodConsumptionResponse expectedResponse = TestEntityFactory.foodConsumptionResponse();
        when(foodConsumptionService.update(id, request, TestEntityFactory.UTC_TIMEZONE)).thenReturn(expectedResponse);
        FoodConsumptionResponse response = restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(id))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodConsumptionResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void putFoodConsumption_shouldReturn400_whenBodyContentIsNull() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(null);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void putFoodConsumption_shouldReturn404_whenResourceNotFound() {
        when(foodConsumptionService.update(anyLong(), any(FoodConsumptionRequest.class), anyString())).thenThrow(
                ResourceNotFoundException.class);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void putFoodConsumption_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        when(foodConsumptionService.update(anyLong(), any(FoodConsumptionRequest.class), anyString())).thenThrow(
                DailyIntakeCannotBeLowerThanZeroException.class);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteById_shouldReturn204() {
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteById_shouldReturn404_whenResourceNotFound() {
        doThrow(ResourceNotFoundException.class)
                .when(foodConsumptionService).deleteById(anyLong(), anyString());
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        doThrow(DailyIntakeCannotBeLowerThanZeroException.class)
                .when(foodConsumptionService).deleteById(anyLong(), anyString());
        restTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .pathSegment(String.valueOf(TestEntityFactory.DEFAULT_ID))
                        .build()
                )
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    private void doAssertionsCheckOnResponse(FoodConsumptionResponse response,
            FoodConsumptionResponse expectedResponse) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.consumedAt()).isEqualTo(expectedResponse.consumedAt());
        Assertions.assertThat(response.amount()).isEqualByComparingTo(expectedResponse.amount());
        Assertions.assertThat(response.phenylalanineAmount()).isEqualByComparingTo(
                expectedResponse.phenylalanineAmount());
    }

}