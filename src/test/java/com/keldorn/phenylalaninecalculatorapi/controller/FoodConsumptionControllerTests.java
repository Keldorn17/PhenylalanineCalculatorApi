package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceAccessDeniedException;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.DailyIntakeCannotBeLowerThanZeroException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodConsumptionNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.FoodConsumptionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(FoodConsumptionController.class)
public class FoodConsumptionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodConsumptionService foodConsumptionService;

    private RestTestClient restTestClient;

    @BeforeEach
    public void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void getAllFoodConsumptionByDate_shouldReturn200() {
        LocalDate testDate = TestEntityFactory.TEST_DATE;
        int page = 0;
        int size = 20;
        FoodConsumptionResponse expectedResponse = TestEntityFactory.foodConsumptionResponse();
        Page<FoodConsumptionResponse> pageResponse = new PageImpl<>(List.of(expectedResponse));
        when(foodConsumptionService.findAllByDate(testDate, page, size)).thenReturn(pageResponse);
        TestPage<FoodConsumptionResponse> response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", testDate)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodConsumptionResponse>>() {})
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.content()).hasSize(1);
        Assertions.assertThat(response.content().getFirst().id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.content().getFirst().amount()).isEqualByComparingTo(expectedResponse.amount());
        Assertions.assertThat(response.content().getFirst().consumedAt()).isEqualTo(expectedResponse.consumedAt());
        Assertions.assertThat(response.content().getFirst().phenylalanineAmount())
                .isEqualByComparingTo(expectedResponse.phenylalanineAmount());
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
        when(foodConsumptionService.save(foodId, request)).thenReturn(expectedResponse);
        FoodConsumptionResponse response = restTestClient.post()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, foodId))
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FoodConsumptionResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.consumedAt()).isEqualTo(expectedResponse.consumedAt());
        Assertions.assertThat(response.amount()).isEqualByComparingTo(expectedResponse.amount());
        Assertions.assertThat(response.phenylalanineAmount()).isEqualByComparingTo(
                expectedResponse.phenylalanineAmount());
    }

    @Test
    void postFoodConsumption_shouldReturn400_whenBodyContentIsNull() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(null);
        restTestClient.post()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void postFoodConsumption_shouldReturn203_whenUserDoesNotOwnResource() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);
        when(foodConsumptionService.save(id, request)).thenThrow(ResourceAccessDeniedException.class);
        restTestClient.post()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void postFoodConsumption_shouldReturn404_whenFoodConsumptionNotFound() {
        when(foodConsumptionService.save(anyLong(), any(FoodConsumptionRequest.class))).thenThrow(
                FoodConsumptionNotFoundException.class);
        restTestClient.post()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postFoodConsumption_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        when(foodConsumptionService.save(anyLong(), any(FoodConsumptionRequest.class))).thenThrow(
                DailyIntakeCannotBeLowerThanZeroException.class);
        restTestClient.post()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void putFoodConsumption_shouldReturn200() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodConsumptionRequest request = new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
        FoodConsumptionResponse expectedResult = TestEntityFactory.foodConsumptionResponse();
        when(foodConsumptionService.update(id, request)).thenReturn(expectedResult);
        FoodConsumptionResponse response = restTestClient.put()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, id))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FoodConsumptionResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResult.id());
        Assertions.assertThat(response.consumedAt()).isEqualTo(expectedResult.consumedAt());
        Assertions.assertThat(response.amount()).isEqualByComparingTo(expectedResult.amount());
        Assertions.assertThat(response.phenylalanineAmount()).isEqualByComparingTo(
                expectedResult.phenylalanineAmount());
    }

    @Test
    void putFoodConsumption_shouldReturn400_whenBodyContentIsNull() {
        FoodConsumptionRequest request = new FoodConsumptionRequest(null);
        restTestClient.put()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void putFoodConsumption_shouldReturn203_whenUserDoesNotOwnResource() {
        Long id = TestEntityFactory.DEFAULT_ID;
        FoodConsumptionRequest request = new FoodConsumptionRequest(BigDecimal.TEN);
        when(foodConsumptionService.update(id, request)).thenThrow(ResourceAccessDeniedException.class);
        restTestClient.put()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void putFoodConsumption_shouldReturn404_whenFoodConsumptionNotFound() {
        when(foodConsumptionService.update(anyLong(), any(FoodConsumptionRequest.class))).thenThrow(
                FoodConsumptionNotFoundException.class);
        restTestClient.put()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void putFoodConsumption_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        when(foodConsumptionService.update(anyLong(), any(FoodConsumptionRequest.class))).thenThrow(
                DailyIntakeCannotBeLowerThanZeroException.class);
        restTestClient.put()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .body(new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void deleteById_shouldReturn204() {
        restTestClient.delete()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteById_shouldReturn403_whenUserDoesNotOwnResource() {
        doThrow(ResourceAccessDeniedException.class)
                .when(foodConsumptionService).deleteById(anyLong());
        restTestClient.delete()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteById_shouldReturn404_whenFoodConsumptionNotFound() {
        doThrow(FoodConsumptionNotFoundException.class)
                .when(foodConsumptionService).deleteById(anyLong());
        restTestClient.delete()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteById_shouldReturn409_whenNegativeDailyIntakeValueInserted() {
        doThrow(DailyIntakeCannotBeLowerThanZeroException.class)
                .when(foodConsumptionService).deleteById(anyLong());
        restTestClient.delete()
                .uri(String.format("%s/%d", ApiRoutes.FOOD_CONSUMPTION_PATH, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().is4xxClientError();
    }

}