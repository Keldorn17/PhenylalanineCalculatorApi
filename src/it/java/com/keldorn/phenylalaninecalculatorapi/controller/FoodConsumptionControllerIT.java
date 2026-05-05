package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.annotation.DirtyTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class FoodConsumptionControllerIT extends BaseIntegrationTest {

    private static final Long NEW_FOOD_CONSUMPTION_ID = 2L;
    private static final Long UNKNOWN_ID = 99L;

    @Test
    void testGetAllFoodConsumptionByDate_shouldReturn200() {
        TestPage<FoodConsumptionResponse> expectedResponse =
                new TestPage<>(List.of(TestEntityFactory.foodConsumptionResponse()),
                        new TestPage.PageMetadata(20, 0, 1, 1));
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", TestEntityFactory.TEST_DATE)
                        .build()
                )
                .headers(withBearer(getAuthToken()))
                .headers(headers -> headers.add("X-Timezone", TestEntityFactory.UTC_TIMEZONE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodConsumptionResponse>>() {})
                .value(actual -> verifySuccess(actual, expectedResponse));
    }

    @Test
    void testGetAllFoodConsumptionByDate_shouldReturn200_withEmptyContent() {
        TestPage<FoodConsumptionResponse> expectedResponse =
                new TestPage<>(List.of(), new TestPage.PageMetadata(20, 0, 0, 0));
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", TestEntityFactory.TEST_DATE.plusDays(1))
                        .build()
                )
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodConsumptionResponse>>() {})
                .value(actual -> verifySuccess(actual, expectedResponse));
    }

    @Test
    void testGetAllFoodConsumptionByDate_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", TestEntityFactory.TEST_DATE.toString())
                        .build()
                )
                .exchange()
                .expectStatus().isUnauthorized();
        verifyResponse(responseSpec, expectedResponse);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("postFoodConsumptionTestCases")
    void testPostFoodConsumption(String description,
            Long foodId,
            FoodConsumptionRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.post()
                .uri(path(ApiRoutes.FOOD_CONSUMPTION_PATH_BY_ID, foodId))
                .headers(withBearer(getAuthToken()))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccessIgnoreCreatedAt(responseSpec, (FoodConsumptionResponse) expectedResponse);
            responseSpec.expectHeader().location(
                    String.valueOf(path(ApiRoutes.FOOD_CONSUMPTION_PATH_BY_ID, NEW_FOOD_CONSUMPTION_ID)));
            return;
        }
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("putFoodConsumptionTestCases")
    void testPutFoodConsumption(String description,
            Long id,
            FoodConsumptionRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.FOOD_CONSUMPTION_PATH_BY_ID, id))
                .headers(withBearer(getAuthToken()))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifyResponseWithBigDecimalCompareTo(responseSpec, (FoodConsumptionResponse) expectedResponse);
            return;
        }
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    @DirtyTest
    @MethodSource("deleteByIdTestCases")
    @ParameterizedTest(name = "{index} = {0}")
    void testDeleteById(String description,
            Long id,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.delete()
                .uri(path(ApiRoutes.FOOD_CONSUMPTION_PATH_BY_ID, id))
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    private static Stream<Arguments> postFoodConsumptionTestCases() {
        return Stream.of(
                Arguments.of("Successful food consumption creation",
                        TestEntityFactory.DEFAULT_ID,
                        foodConsumptionRequest(),
                        HttpStatus.CREATED,
                        foodConsumptionResponse(NEW_FOOD_CONSUMPTION_ID, BigDecimal.TEN, BigDecimal.valueOf(0.1))
                ),
                Arguments.of("Food not found",
                        UNKNOWN_ID,
                        foodConsumptionRequest(),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                ),
                Arguments.of("Conflict: Negative food consumption",
                        TestEntityFactory.DEFAULT_ID,
                        new FoodConsumptionRequest(BigDecimal.valueOf(-1)),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.DAILY_INTAKE_NEGATIVE_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> putFoodConsumptionTestCases() {
        return Stream.of(
                Arguments.of("Successful food consumption update",
                        TestEntityFactory.DEFAULT_ID,
                        foodConsumptionRequest(),
                        HttpStatus.OK,
                        foodConsumptionResponse(TestEntityFactory.DEFAULT_ID,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE, BigDecimal.valueOf(0.1))
                ),
                Arguments.of("Conflict: Negative overall food consumption",
                        TestEntityFactory.DEFAULT_ID,
                        new FoodConsumptionRequest(BigDecimal.valueOf(-100)),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.DAILY_INTAKE_NEGATIVE_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> deleteByIdTestCases() {
        return Stream.of(
                Arguments.of("Successful deletion by id",
                        TestEntityFactory.DEFAULT_ID,
                        HttpStatus.NO_CONTENT,
                        null
                ),
                Arguments.of("Food consumption not found",
                        UNKNOWN_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static @NonNull FoodConsumptionRequest foodConsumptionRequest() {
        return new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
    }

    private static @NonNull FoodConsumptionResponse foodConsumptionResponse(Long id, BigDecimal amount,
            BigDecimal phenylalanineAmount) {
        return new FoodConsumptionResponse(id, TestEntityFactory.DEFAULT_FOOD_NAME, amount, phenylalanineAmount,
                TestEntityFactory.TEST_DATE_TIME);
    }

    private static void verifySuccess(TestPage<FoodConsumptionResponse> actual,
            TestPage<FoodConsumptionResponse> expectedResponse) {
        Assertions.assertThat(actual).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedResponse);
    }

    private static void verifySuccessIgnoreCreatedAt(RestTestClient.ResponseSpec responseSpec,
            FoodConsumptionResponse expectedResponse) {
        responseSpec.expectBody(FoodConsumptionResponse.class).value(
                actual -> Assertions.assertThat(actual).usingRecursiveComparison()
                        .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .ignoringFieldsOfTypes(LocalDateTime.class)
                        .isEqualTo(expectedResponse));
    }

}
