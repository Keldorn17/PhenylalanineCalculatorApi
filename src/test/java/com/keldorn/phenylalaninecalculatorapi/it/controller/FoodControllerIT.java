package com.keldorn.phenylalaninecalculatorapi.it.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.it.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.it.annotation.DirtyTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class FoodControllerIT extends BaseIntegrationTest {

    private static final Long UNKNOWN_ID = 99L;
    private static final Long UPDATE_FOOD_TYPE_ID = 2L;
    private static final String UPDATE_NAME = "updateName";
    private static final BigDecimal UPDATE_BIGDECIMAL = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    @MethodSource("getByIdTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testGetById(String description,
            Long foodId,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.get()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, foodId))
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, (FoodResponse) expectedResponse);
            return;
        }
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    void testGetById_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    void testGetAll_shouldReturn200() {
        TestPage<FoodResponse> expectedResponse =
                new TestPage<>(List.of(foodResponse()), new TestPage.PageMetadata(20, 0, 1, 1));
        restTestClient.get()
                .uri(ApiRoutes.FOOD_PATH)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodResponse>>() {})
                .value(actual -> verifySuccess(actual, expectedResponse));
    }

    @DirtyTest
    @MethodSource("postFoodTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testPostFood(String description,
            FoodRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.post()
                .uri(ApiRoutes.FOOD_PATH)
                .body(request)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, (FoodResponse) expectedResponse);
            responseSpec.expectHeader().location(String.valueOf(path(ApiRoutes.FOOD_PATH_BY_ID, 2L)));
            return;
        }
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    @DirtyTest
    void testPostFood_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        FoodRequest request = new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_ID);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.post()
                .uri(ApiRoutes.FOOD_PATH)
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @DirtyTest
    @MethodSource("patchFoodTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testPatchFood(String description,
            Long foodId,
            FoodUpdateRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.patch()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, foodId))
                .body(request)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, (FoodResponse) expectedResponse);
            return;
        }
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    void testPatchFood_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        FoodUpdateRequest request =
                new FoodUpdateRequest(UPDATE_NAME, UPDATE_BIGDECIMAL, UPDATE_BIGDECIMAL, UPDATE_FOOD_TYPE_ID);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.patch()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, 1L))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @DirtyTest
    @MethodSource("deleteByIdTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testDeleteById(String description,
            Long foodId,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.delete()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, foodId))
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    void testDeleteById_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.delete()
                .uri(path(ApiRoutes.FOOD_PATH_BY_ID, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    private static Stream<Arguments> getByIdTestCases() {
        return Stream.of(
                Arguments.of("Successful food retrieval",
                        TestEntityFactory.DEFAULT_ID,
                        HttpStatus.OK,
                        foodResponse()
                ),
                Arguments.of("Food not found by id",
                        UNKNOWN_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> postFoodTestCases() {
        return Stream.of(
                Arguments.of("Successful food creation",
                        new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_ID),
                        HttpStatus.CREATED,
                        foodResponse(2L,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                BigDecimal.valueOf(100L).setScale(4, RoundingMode.HALF_UP))
                ),
                Arguments.of("Food name missing",
                        new FoodRequest(null,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_ID),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("name"))
                ),
                Arguments.of("Food protein missing",
                        new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                                null,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_ID),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST,
                                ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("protein"))
                ),
                Arguments.of("Food calories missing",
                        new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                null,
                                TestEntityFactory.DEFAULT_ID),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST,
                                ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("calories"))
                ),
                Arguments.of("Food type id missing",
                        new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST,
                                ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("foodTypeId"))
                ),
                Arguments.of("Food type not found",
                        new FoodRequest(TestEntityFactory.DEFAULT_FOOD_NAME,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                                UNKNOWN_ID),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> patchFoodTestCases() {
        return Stream.of(
                Arguments.of("Successful update",
                        TestEntityFactory.DEFAULT_ID,
                        new FoodUpdateRequest(UPDATE_NAME, UPDATE_BIGDECIMAL, UPDATE_BIGDECIMAL, UPDATE_FOOD_TYPE_ID),
                        HttpStatus.OK,
                        foodResponse(TestEntityFactory.DEFAULT_ID,
                                UPDATE_NAME,
                                UPDATE_BIGDECIMAL,
                                UPDATE_BIGDECIMAL,
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE_SCALE_2)
                ),
                Arguments.of("Food type not found",
                        TestEntityFactory.DEFAULT_ID,
                        new FoodUpdateRequest(UPDATE_NAME, UPDATE_BIGDECIMAL, UPDATE_BIGDECIMAL, UNKNOWN_ID),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> deleteByIdTestCases() {
        return Stream.of(
                Arguments.of("Successful deletion",
                        TestEntityFactory.DEFAULT_ID,
                        HttpStatus.NO_CONTENT,
                        null
                ),
                Arguments.of("Food not found",
                        UNKNOWN_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static FoodResponse foodResponse(Long id, String name, BigDecimal protein, BigDecimal calories,
            BigDecimal phenylalanine) {
        return new FoodResponse(id,
                name,
                protein,
                calories,
                phenylalanine,
                TestEntityFactory.DEFAULT_FOOD_TYPE_NAME,
                TestEntityFactory.DEFAULT_INTEGER_VALUE);
    }

    private static FoodResponse foodResponse(Long id, BigDecimal protein, BigDecimal calories,
            BigDecimal phenylalanine) {
        return foodResponse(id, TestEntityFactory.DEFAULT_FOOD_NAME, protein, calories, phenylalanine);
    }

    private static FoodResponse foodResponse() {
        return foodResponse(TestEntityFactory.DEFAULT_ID,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE_SCALE_2,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE_SCALE_2,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE_SCALE_2);
    }

    private static void verifySuccess(RestTestClient.ResponseSpec responseSpec, FoodResponse expectedResponse) {
        responseSpec.expectBody(FoodResponse.class)
                .value(actual -> Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedResponse));
    }

    private static void verifySuccess(TestPage<FoodResponse> actual, TestPage<FoodResponse> expectedResponse) {
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

}
