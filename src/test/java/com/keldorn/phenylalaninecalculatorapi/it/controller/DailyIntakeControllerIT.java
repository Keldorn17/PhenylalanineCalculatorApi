package com.keldorn.phenylalaninecalculatorapi.it.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.it.BaseIntegrationTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class DailyIntakeControllerIT extends BaseIntegrationTest {

    private static final LocalDate REGISTERED_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate UNREGISTERED_DATE = LocalDate.of(2026, 4, 3);
    private static final String MALFORMED_DATE = "malformed date";

    @MethodSource("getDailyIntakeTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testGetDailyIntake(String description,
            String date,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        String token = getAuthToken();
        var responseSpec = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.DAILY_INTAKE_PATH)
                        .queryParam("date", date)
                        .build()
                )
                .headers(withBearer(token))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, (DailyIntakeResponse) expectedResponse);
            return;
        }
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    void testGetDailyIntake_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.DAILY_INTAKE_PATH)
                        .queryParam("date", REGISTERED_DATE)
                        .build()
                )
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    private static Stream<Arguments> getDailyIntakeTestCases() {
        return Stream.of(
                Arguments.of("Successful daily intake retrieval",
                        REGISTERED_DATE.toString(),
                        HttpStatus.OK,
                        new DailyIntakeResponse(1L, REGISTERED_DATE, TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE)
                ),
                Arguments.of("Not found for specified day",
                        UNREGISTERED_DATE.toString(),
                        HttpStatus.NOT_FOUND,
                        error(HttpStatus.NOT_FOUND, ApiResponses.RESOURCE_NOT_FOUND_RESPONSE)
                ),
                Arguments.of("Malformed Date received",
                        MALFORMED_DATE,
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.MALFORMED_RESPONSE)
                ),
                Arguments.of("Missing Date parameter",
                        null,
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.REQUIRED_MISSING_RESPONSE.formatted("date"))
                )
        );
    }

    private void verifySuccess(RestTestClient.ResponseSpec spec, DailyIntakeResponse expected) {
        spec.expectBody(DailyIntakeResponse.class)
                .value(actual -> Assertions.assertThat(actual).usingRecursiveComparison()
                        .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                        .isEqualTo(expected));
    }

}
