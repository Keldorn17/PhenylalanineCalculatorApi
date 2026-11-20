package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.annotation.DirtyTest;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.TestPage;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionCreateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
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

class FoodConsumptionControllerIT extends BaseIntegrationTest {

    private static final Long NEW_FOOD_CONSUMPTION_ID = 2L;
    private static final Long PRIVATE_FOOD_ID = 5L;
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
                .headers(withBearer(getAuthToken().accessToken()))
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
                .headers(withBearer(getAuthToken().accessToken()))
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
    @MethodSource("earlyMorningTimezoneTestCases")
    void testPostAndGetFoodConsumption_shouldKeepEarlyMorningEntriesOnLocalDay(String description,
            ZonedDateTime consumedAt,
            String expectedLocalDate,
            String previousLocalDate,
            String timezone) {
        String token = getAuthToken().accessToken();
        restTestClient.post()
                .uri(ApiRoutes.FOOD_CONSUMPTION_PATH)
                .headers(withBearer(token))
                .body(new FoodConsumptionCreateRequest(TestEntityFactory.DEFAULT_ID, BigDecimal.TEN, consumedAt))
                .exchange()
                .expectStatus().isCreated();
        assertFoodConsumptionCount(token, expectedLocalDate, timezone, 1);
        assertFoodConsumptionCount(token, previousLocalDate, timezone, 0);
        assertDailyIntakeAmount(token, expectedLocalDate, timezone, BigDecimal.ONE);
        assertDailyIntakeAmount(token, previousLocalDate, timezone, BigDecimal.ZERO);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("postFoodConsumptionTestCases")
    void testPostFoodConsumption(String description,
            FoodConsumptionCreateRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.post()
                .uri(ApiRoutes.FOOD_CONSUMPTION_PATH)
                .headers(withBearer(getAuthToken().accessToken()))
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
                .headers(withBearer(getAuthToken().accessToken()))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifyResponse(responseSpec, (FoodConsumptionResponse) expectedResponse);
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
                .headers(withBearer(getAuthToken().accessToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        verifyResponse(responseSpec, (ErrorResponse) expectedResponse);
    }

    private static Stream<Arguments> postFoodConsumptionTestCases() {
        return Stream.of(
                Arguments.of("Successful food consumption creation",
                        foodConsumptionCreateRequest(TestEntityFactory.DEFAULT_ID),
                        HttpStatus.CREATED,
                        foodConsumptionResponse(NEW_FOOD_CONSUMPTION_ID, BigDecimal.TEN)
                ),
                Arguments.of("Food not found",
                        foodConsumptionCreateRequest(UNKNOWN_ID),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                ),
                Arguments.of("Private food of another user not found",
                        foodConsumptionCreateRequest(PRIVATE_FOOD_ID),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
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
                                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE)
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

    private static Stream<Arguments> earlyMorningTimezoneTestCases() {
        return Stream.of(
                Arguments.of("00:30 CET stays on local day",
                        ZonedDateTime.parse("2026-11-20T00:30:00+01:00"),
                        "2026-11-20",
                        "2026-11-19",
                        "Europe/Budapest"),
                Arguments.of("01:00 CEST stays on local day",
                        ZonedDateTime.parse("2026-07-20T01:00:00+02:00"),
                        "2026-07-20",
                        "2026-07-19",
                        "Europe/Budapest")
        );
    }

    private static @NonNull FoodConsumptionCreateRequest foodConsumptionCreateRequest(Long foodId) {
        return new FoodConsumptionCreateRequest(foodId, TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
    }

    private static @NonNull FoodConsumptionRequest foodConsumptionRequest() {
        return new FoodConsumptionRequest(TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE);
    }

    private static @NonNull FoodConsumptionResponse foodConsumptionResponse(Long id, BigDecimal amount) {
        return new FoodConsumptionResponse(id, TestEntityFactory.DEFAULT_ID, TestEntityFactory.DEFAULT_FOOD_NAME,
                amount, BigDecimal.ONE,
                TestEntityFactory.TEST_ZONED_DATE_TIME);
    }

    private void assertFoodConsumptionCount(String token, String date, String timezone, int expectedSize) {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.FOOD_CONSUMPTION_PATH)
                        .queryParam("date", date)
                        .queryParam("timezone", timezone)
                        .build()
                )
                .headers(withBearer(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<TestPage<FoodConsumptionResponse>>() {})
                .value(page -> {
                    Assertions.assertThat(page).isNotNull();
                    Assertions.assertThat(page.content()).hasSize(expectedSize);
                });
    }

    private void assertDailyIntakeAmount(String token, String date, String timezone, BigDecimal expectedAmount) {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.DAILY_INTAKE_PATH)
                        .queryParam("date", date)
                        .queryParam("timezone", timezone)
                        .build()
                )
                .headers(withBearer(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DailyIntakeResponse.class)
                .value(intake -> {
                    Assertions.assertThat(intake).isNotNull();
                    Assertions.assertThat(intake.consumedPhenylalanine()).isEqualByComparingTo(expectedAmount);
                });
    }

    private static void verifySuccess(TestPage<FoodConsumptionResponse> actual,
            TestPage<FoodConsumptionResponse> expectedResponse) {
        Assertions.assertThat(actual).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .withComparatorForType(ChronoZonedDateTime::compareTo, ZonedDateTime.class)
                .isEqualTo(expectedResponse);
    }

    private static void verifySuccessIgnoreCreatedAt(RestTestClient.ResponseSpec responseSpec,
            FoodConsumptionResponse expectedResponse) {
        responseSpec.expectBody(FoodConsumptionResponse.class).value(
                actual -> Assertions.assertThat(actual).usingRecursiveComparison()
                        .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .ignoringFieldsOfTypes(ZonedDateTime.class)
                        .isEqualTo(expectedResponse));
    }

}
