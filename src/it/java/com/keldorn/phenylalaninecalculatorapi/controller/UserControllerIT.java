package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.annotation.DirtyTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class UserControllerIT extends BaseIntegrationTest {

    private static final String UPDATED_EMAIL = "updated@mail.com";
    private static final BigDecimal UPDATED_DAILY_LIMIT = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
    private static final String INVALID_EMAIL = "invalid email";

    @Test
    void testMe_shouldReturn200() {
        UserResponse expectedResponse = userResponse();
        var responseSpec = restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isOk();
        verifySuccess(responseSpec, expectedResponse);
    }

    @Test
    void testMe_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("updateUserTestCases")
    void testUpdateUser(String description,
            UserRequest request,
            HttpStatus expectedStatus,
            Object expectedResponse) {
        var responseSpec = restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, (UserResponse) expectedResponse);
            return;
        }
        verifyError(responseSpec, (ErrorResponse) expectedResponse);
    }

    @Test
    @DirtyTest
    void testUpdateUser_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        UserRequest request = new UserRequest(UPDATED_EMAIL, UPDATED_DAILY_LIMIT);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    @DirtyTest
    void testDelete_shouldReturn204() {
        restTestClient.delete()
                .uri(ApiRoutes.USER_PATH)
                .headers(withBearer(getAuthToken()))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DirtyTest
    void testDelete_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    private static Stream<Arguments> updateUserTestCases() {
        return Stream.of(
                Arguments.of("Successful all args update",
                        new UserRequest(UPDATED_EMAIL, UPDATED_DAILY_LIMIT),
                        HttpStatus.OK,
                        userResponse(UPDATED_EMAIL, UPDATED_DAILY_LIMIT)
                ),
                Arguments.of("Email is taken",
                        new UserRequest(TestEntityFactory.DEFAULT_EMAIL, null),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.EMAIL_IS_TAKEN_RESPONSE)
                ),
                Arguments.of("Invalid email passed",
                        new UserRequest(INVALID_EMAIL, null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.MALFORMED_EMAIL_RESPONSE)
                )
        );
    }

    private void verifySuccess(RestTestClient.ResponseSpec spec, UserResponse expected) {
        spec.expectBody(UserResponse.class)
                .value(actual -> Assertions.assertThat(actual).usingRecursiveComparison()).isEqualTo(expected);
    }

    private static UserResponse userResponse(String email, BigDecimal dailyLimit) {
        return new UserResponse(TestEntityFactory.DEFAULT_ID,
                TestEntityFactory.DEFAULT_USERNAME,
                email,
                dailyLimit);
    }

    private static UserResponse userResponse() {
        return userResponse(TestEntityFactory.DEFAULT_EMAIL,
                BigDecimal.valueOf(400L).setScale(2, RoundingMode.HALF_UP));
    }

}
