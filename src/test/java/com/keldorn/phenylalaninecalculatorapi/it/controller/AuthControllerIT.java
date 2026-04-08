package com.keldorn.phenylalaninecalculatorapi.it.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiPaths;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.it.BaseIntegrationTest;
import com.keldorn.phenylalaninecalculatorapi.it.annotation.DirtyTest;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import com.keldorn.phenylalaninecalculatorapi.service.DeleteUserAssociationsService;
import com.keldorn.phenylalaninecalculatorapi.service.JwtService;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeleteUserAssociationsService deleteUserAssociationsService;

    @Autowired
    private JwtService jwtService;

    private static final String NOT_TAKEN_EMAIL = "not_taken_email@gmail.com";
    private static final String NOT_TAKEN_USERNAME = "not taken username";
    private static final String INVALID_PASSWORD = "invalid password";
    private static final String TEST_REGISTER_EMAIL = "test@test.com";
    private static final String TEST_REGISTER_USERNAME = "test";
    private static final String NEW_PASSWORD = "new password";
    private static final String NEW_USERNAME = "new username";

    private static final String MISSING_EMAIL_RESPONSE =
            ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("email");
    private static final String MISSING_TIMEZONE_RESPONSE =
            ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("timezone");
    private static final String MISSING_USERNAME_RESPONSE =
            ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("username");
    private static final String MISSING_PASSWORD_RESPONSE =
            ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("password");
    private static final String MISSING_OLD_PASSWORD_RESPONSE =
            ApiResponses.REQUIRED_MISSING_REQUEST_RESPONSE.formatted("oldPassword");

    @Test
    void testRegistration_shouldReturn200_whenSuccessfulRegistration() {
        AuthResponse response = registerTestUser();
        verifySuccess(response, TEST_REGISTER_USERNAME);
    }

    @DirtyTest
    @MethodSource("getRegistrationTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testRegistration(String description,
            AuthRegisterRequest request,
            HttpStatus expectedStatus,
            ErrorResponse expectedResponse) {
        registerTestUser();
        var responseSpec = restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, TEST_REGISTER_USERNAME);
            return;
        }
        verifyError(responseSpec, expectedResponse);
    }

    @MethodSource("getAuthenticateTestCases")
    @ParameterizedTest(name = "{index} - {0}")
    void testAuthenticate(String description,
            AuthRequest request,
            HttpStatus expectedStatus,
            ErrorResponse expectedResponse) {
        var responseSpec = restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec);
            return;
        }
        verifyError(responseSpec, expectedResponse);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("getChangePasswordTestCases")
    void testChangePassword(String description,
            AuthPasswordChangeRequest request,
            HttpStatus expectedStatus,
            ErrorResponse expectedResponse) {
        String token = getAuthToken();
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .headers(withBearer(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec);
            restTestClient.post()
                    .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                    .body(new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, request.password()))
                    .exchange()
                    .expectStatus().isOk();
            return;
        }
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    void testChangePassword_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, NEW_PASSWORD);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    @DirtyTest
    void testChangePassword_shouldReturn401FromSecurityLayer_whenZombieTokenReceived() {
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, NEW_PASSWORD);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        String token = getAuthToken();
        wipeDatabase();
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .headers(withBearer(token))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @DirtyTest
    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("getChangeUsernameTestCases")
    void testChangeUsername(String description,
            AuthUsernameChangeRequest request,
            HttpStatus expectedStatus,
            ErrorResponse expectedResponse) {
        String token = getAuthToken();
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .headers(withBearer(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
        if (expectedStatus.is2xxSuccessful()) {
            verifySuccess(responseSpec, request.username());
            restTestClient.post()
                    .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                    .body(new AuthRequest(request.username(), request.password()))
                    .exchange()
                    .expectStatus().isOk();
            return;
        }
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    void testChangeUsername_shouldReturn401FromSecurityLayer_whenTokenIsMissing() {
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(NEW_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    @Test
    @DirtyTest
    void testChangeUsername_shouldReturn401FromSecurityLayer_whenZombieTokenReceived() {
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(NEW_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        ErrorResponse expectedResponse = error(HttpStatus.UNAUTHORIZED, ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE);
        String token = getAuthToken();
        wipeDatabase();
        var responseSpec = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .headers(withBearer(token))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
        verifyError(responseSpec, expectedResponse);
    }

    private static Stream<Arguments> getRegistrationTestCases() {
        return Stream.of(
                Arguments.of("Email is missing from request",
                        new AuthRegisterRequest(null,
                                TestEntityFactory.DEFAULT_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_EMAIL_RESPONSE)
                ),
                Arguments.of("Username is missing from request",
                        new AuthRegisterRequest(TestEntityFactory.DEFAULT_EMAIL,
                                null,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_USERNAME_RESPONSE)
                ),
                Arguments.of("Password is missing from request",
                        new AuthRegisterRequest(TestEntityFactory.DEFAULT_EMAIL,
                                TestEntityFactory.DEFAULT_USERNAME,
                                null,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_PASSWORD_RESPONSE)
                ),
                Arguments.of("Timezone is missing from request",
                        new AuthRegisterRequest(TestEntityFactory.DEFAULT_EMAIL,
                                TestEntityFactory.DEFAULT_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                null
                        ),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_TIMEZONE_RESPONSE)
                ),
                Arguments.of("Invalid email provided",
                        new AuthRegisterRequest("invalid email",
                                TestEntityFactory.DEFAULT_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, ApiResponses.MALFORMED_EMAIL_RESPONSE)
                ),
                Arguments.of("Email is taken",
                        new AuthRegisterRequest(TestEntityFactory.DEFAULT_EMAIL,
                                NOT_TAKEN_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.EMAIL_IS_TAKEN_RESPONSE)
                ),
                Arguments.of("Username is taken",
                        new AuthRegisterRequest(NOT_TAKEN_EMAIL,
                                TestEntityFactory.DEFAULT_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_TIMEZONE
                        ),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.USERNAME_IS_TAKEN_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> getAuthenticateTestCases() {
        return Stream.of(
                Arguments.of("Test user with correct credentials",
                        new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.OK,
                        null
                ),
                Arguments.of("Username is missing from request",
                        new AuthRequest(null, TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_USERNAME_RESPONSE)
                ),
                Arguments.of("Password is missing from request",
                        new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_PASSWORD_RESPONSE)
                ),
                Arguments.of("Unknown credentials received",
                        new AuthRequest("unknown user", TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.UNAUTHORIZED,
                        error(HttpStatus.UNAUTHORIZED, ApiResponses.UNAUTHORIZED_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> getChangePasswordTestCases() {
        return Stream.of(
                Arguments.of("Successful password change",
                        new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, NEW_PASSWORD),
                        HttpStatus.OK,
                        null
                ),
                Arguments.of("Old password is missing from request",
                        new AuthPasswordChangeRequest(null, NEW_PASSWORD),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_OLD_PASSWORD_RESPONSE)
                ),
                Arguments.of("Password is missing from request",
                        new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_PASSWORD_RESPONSE)
                ),
                Arguments.of("Both password are the same",
                        new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD,
                                TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.PASSWORD_MISMATCH_RESPONSE)
                ),
                Arguments.of("Test user's password not match the provided old password",
                        new AuthPasswordChangeRequest(INVALID_PASSWORD, NEW_PASSWORD),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.PASSWORD_MISMATCH_RESPONSE)
                )
        );
    }

    private static Stream<Arguments> getChangeUsernameTestCases() {
        return Stream.of(
                Arguments.of("Successful username change",
                        new AuthUsernameChangeRequest(NEW_USERNAME, TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.OK,
                        null
                ),
                Arguments.of("Username is missing from request",
                        new AuthUsernameChangeRequest(null, TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_USERNAME_RESPONSE)
                ),
                Arguments.of("Password is missing from request",
                        new AuthUsernameChangeRequest(NEW_USERNAME, null),
                        HttpStatus.BAD_REQUEST,
                        error(HttpStatus.BAD_REQUEST, MISSING_PASSWORD_RESPONSE)
                ),
                Arguments.of("Changing to taken username",
                        new AuthUsernameChangeRequest(TestEntityFactory.DEFAULT_USERNAME,
                                TestEntityFactory.DEFAULT_PASSWORD),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.USERNAME_IS_TAKEN_RESPONSE)
                ),
                Arguments.of("Test user's password not match the provided password",
                        new AuthUsernameChangeRequest(NEW_USERNAME, INVALID_PASSWORD),
                        HttpStatus.CONFLICT,
                        error(HttpStatus.CONFLICT, ApiResponses.PASSWORD_MISMATCH_RESPONSE)
                )
        );
    }

    private void verifySuccess(AuthResponse response, String registeredUsername) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isNotEmpty();
        String username = jwtService.extractUsername(response.token());
        Assertions.assertThat(username).isEqualTo(registeredUsername);
    }

    private void verifySuccess(RestTestClient.ResponseSpec spec, String registeredUsername) {
        spec.expectBody(AuthResponse.class)
                .value(actual -> verifySuccess(actual, registeredUsername));
    }

    private void verifySuccess(RestTestClient.ResponseSpec spec) {
        verifySuccess(spec, TestEntityFactory.DEFAULT_USERNAME);
    }

    private AuthResponse registerTestUser() {
        return restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(new AuthRegisterRequest(
                        TEST_REGISTER_EMAIL,
                        TEST_REGISTER_USERNAME,
                        TestEntityFactory.DEFAULT_PASSWORD,
                        TestEntityFactory.DEFAULT_TIMEZONE
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void wipeDatabase() {
        Long userId = TestEntityFactory.DEFAULT_ID;
        deleteUserAssociationsService.removeAssociation(userId);
        userRepository.deleteById(userId);
    }

}
