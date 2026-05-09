package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.annotation.DirtyTest;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;

class FoodTypeControllerIT extends BaseIntegrationTest {

    private static final Long EXISTING_FOOD_TYPE_ID = 1L;
    private static final Long UNOWNED_FOOD_TYPE_ID = 2L;
    private static final Long UNKNOWN_FOOD_TYPE_ID = 99L;
    private static final Long NEXT_AVAILABLE_FOOD_TYPE_ID = 4L;
    private static final String UPDATED_FOOD_TYPE_NAME = "updated name";
    private static final Integer UPDATED_FOOD_TYPE_MULTIPLIER = 15;

    @MethodSource("findByIdTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testFindById(String description,
            Long foodTypeId,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.get()
                .uri(path(ApiRoutes.FOOD_TYPE_PATH_BY_ID, foodTypeId))
                .headers(withBearer(getAuthToken().accessToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifyResponse(responseSpec, (FoodTypeResponse) expectedResponse);
            return;
        }
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    void testFindById_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(path(ApiRoutes.FOOD_TYPE_PATH_BY_ID, TestEntityFactory.DEFAULT_ID))
                .exchange()
                .expectStatus().isUnauthorized();
        verifyResponse(responseSpec, expectedResponse);
    }

    @Test
    void testFindAll_shouldReturn200() {
        TestPage<FoodTypeResponse> expectedResponse =
                new TestPage<>(List.of(
                        TestEntityFactory.foodTypeResponse(),
                        foodTypeResponse(2L, false)
                ), new TestPage.PageMetadata(20, 0, 2, 1));
        restTestClient.get()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .headers(withBearer(getAuthToken().accessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodTypeResponse>>() {})
                .value(actual -> verifySuccess(actual, expectedResponse));
    }

    @DirtyTest
    @MethodSource("postFoodTypeTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testPostFoodType(String description,
            FoodTypeRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.post()
                .uri(ApiRoutes.FOOD_TYPE_PATH)
                .body(request)
                .headers(withBearer(getAuthToken().accessToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifyResponse(responseSpec, (FoodTypeResponse) expectedResponse);
            responseSpec.expectHeader().location(
                    String.valueOf(path(ApiRoutes.FOOD_TYPE_PATH_BY_ID, NEXT_AVAILABLE_FOOD_TYPE_ID)));
            return;
        }
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    @DirtyTest
    @MethodSource("putFoodTypeTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testPutFoodType(String description,
            FoodTypeRequest request,
            Long foodTypeId,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.FOOD_TYPE_PATH_BY_ID, foodTypeId))
                .headers(withBearer(getAuthToken().accessToken()))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifyResponse(responseSpec, (FoodTypeResponse) expectedResponse);
            return;
        }
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    @DirtyTest
    @MethodSource("deleteByIdTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testDeleteById(String description,
            Long foodTypeId,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.delete()
                .uri(path(ApiRoutes.FOOD_TYPE_PATH_BY_ID, foodTypeId))
                .headers(withBearer(getAuthToken().accessToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    private static Stream<Arguments> findByIdTestCases() {
        return Stream.of(
                Arguments.of("Successful food type retrieval",
                        EXISTING_FOOD_TYPE_ID,
                        HttpStatus.OK,
                        TestEntityFactory.foodTypeResponse()
                ),
                Arguments.of("Food type not found by id",
                        UNKNOWN_FOOD_TYPE_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> postFoodTypeTestCases() {
        return Stream.of(
                Arguments.of("Successful food type creation",
                        foodTypeRequest(),
                        HttpStatus.CREATED,
                        foodTypeResponse(NEXT_AVAILABLE_FOOD_TYPE_ID, true)
                ),
                Arguments.of("Missing name parameter",
                        new FoodTypeRequest(null, TestEntityFactory.DEFAULT_INTEGER_VALUE),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.MUST_NOT_BE_BLANK_RESPONSE.formatted("name"))
                ),
                Arguments.of("Missing multiplier parameter",
                        new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST,
                                ApiResponses.MUST_NOT_BE_NULL_RESPONSE.formatted("multiplier"))
                )
        );
    }

    private static Stream<Arguments> putFoodTypeTestCases() {
        return Stream.of(
                Arguments.of("Successful food type update",
                        new FoodTypeRequest(UPDATED_FOOD_TYPE_NAME, UPDATED_FOOD_TYPE_MULTIPLIER),
                        EXISTING_FOOD_TYPE_ID,
                        HttpStatus.OK,
                        new FoodTypeResponse(EXISTING_FOOD_TYPE_ID, UPDATED_FOOD_TYPE_NAME,
                                UPDATED_FOOD_TYPE_MULTIPLIER, true)
                ),
                Arguments.of("Food type not found",
                        foodTypeRequest(),
                        UNKNOWN_FOOD_TYPE_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                ),
                Arguments.of("Missing name parameter",
                        new FoodTypeRequest(null, TestEntityFactory.DEFAULT_INTEGER_VALUE),
                        EXISTING_FOOD_TYPE_ID,
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.MUST_NOT_BE_BLANK_RESPONSE.formatted("name"))
                ),
                Arguments.of("Missing multiplier parameter",
                        new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, null),
                        EXISTING_FOOD_TYPE_ID,
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST,
                                ApiResponses.MUST_NOT_BE_NULL_RESPONSE.formatted("multiplier"))
                ),
                Arguments.of("Cannot edit unowned resource",
                        new FoodTypeRequest(UPDATED_FOOD_TYPE_NAME, UPDATED_FOOD_TYPE_MULTIPLIER),
                        UNOWNED_FOOD_TYPE_ID,
                        HttpStatus.FORBIDDEN,
                        error(HttpStatus.FORBIDDEN, ApiResponses.UNOWNED_RESOURCE_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> deleteByIdTestCases() {
        return Stream.of(
                Arguments.of("Successful food type deletion by id",
                        EXISTING_FOOD_TYPE_ID,
                        HttpStatus.NO_CONTENT,
                        null
                ),
                Arguments.of("Food type not found",
                        UNKNOWN_FOOD_TYPE_ID,
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                ),
                Arguments.of("Cannot delete unowned resource",
                        UNOWNED_FOOD_TYPE_ID,
                        HttpStatus.FORBIDDEN,
                        error(HttpStatus.FORBIDDEN, ApiResponses.UNOWNED_RESOURCE_RESPONSE)
                )
        );
    }

    private static FoodTypeRequest foodTypeRequest() {
        return new FoodTypeRequest(TestEntityFactory.DEFAULT_FOOD_TYPE_NAME, TestEntityFactory.DEFAULT_INTEGER_VALUE);
    }

    private static FoodTypeResponse foodTypeResponse(Long foodTypeId, boolean canEdit) {
        return new FoodTypeResponse(foodTypeId, TestEntityFactory.DEFAULT_FOOD_TYPE_NAME,
                TestEntityFactory.DEFAULT_INTEGER_VALUE, canEdit);
    }

    private static void verifySuccess(TestPage<FoodTypeResponse> actual, TestPage<FoodTypeResponse> expectedResponse) {
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

}
