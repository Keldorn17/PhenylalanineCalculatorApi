package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiPaths;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.AuthService;
import com.keldorn.phenylalaninecalculatorapi.utils.RestTestUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@WebMvcTest(AuthController.class)
public class AuthControllerTests extends RestTestUtils {

    @MockitoBean
    private AuthService authService;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void authenticate_shouldReturn200() {
        AuthResponse expectedResponse = new AuthResponse("Test Token");
        AuthRequest request = new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.authenticate(request)).thenReturn(expectedResponse);
        AuthResponse response = restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isEqualTo(expectedResponse.token());
    }

    @Test
    void authenticate_shouldReturn400_whenRequiredDataIsMissing() {
        AuthRequest request = new AuthRequest(null, null);
        restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticate_shouldReturn404_whenResourceNotFound() {
        AuthRequest request = new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.authenticate(request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void register_shouldReturn200() {
        AuthResponse expectedResponse = new AuthResponse("Test Token");
        AuthRegisterRequest request = new AuthRegisterRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_USERNAME,
                TestEntityFactory.DEFAULT_PASSWORD
        );
        when(authService.register(request)).thenReturn(expectedResponse);
        AuthResponse response = restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isEqualTo(expectedResponse.token());
    }

    @Test
    void register_shouldReturn400_whenRequiredDataIsMissing() {
        AuthRegisterRequest request = new AuthRegisterRequest(null, null, null);
        restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void register_shouldReturn409_whenEmailIsTaken() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_USERNAME,
                TestEntityFactory.DEFAULT_PASSWORD
        );
        when(authService.register(request)).thenThrow(EmailIsTakenException.class);
        restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_shouldReturn409_whenUsernameIsTaken() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_USERNAME,
                TestEntityFactory.DEFAULT_PASSWORD
        );
        when(authService.register(request)).thenThrow(UsernameIsTakenException.class);
        restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changePassword_shouldReturn200() {
        AuthResponse expectedResponse = new AuthResponse("Test Token");
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changePassword(request)).thenReturn(expectedResponse);
        AuthResponse response = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isEqualTo(expectedResponse.token());
    }

    @Test
    void changePassword_shouldReturn400_whenRequiredDataIsMissing() {
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest(null, null);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void changePassword_shouldReturn404_whenResourceNotFound() {
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changePassword(request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void changePassword_shouldReturn409_whenPasswordMismatch() {
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changePassword(request)).thenThrow(PasswordMismatchException.class);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.PASSWORD))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changeUsername_shouldReturn200() {
        AuthResponse expectedResponse = new AuthResponse("Test Token");
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changeUsername(request)).thenReturn(expectedResponse);
        AuthResponse response = restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isEqualTo(expectedResponse.token());
    }

    @Test
    void changeUsername_shouldReturn400_whenRequiredDataIsMissing() {
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(null, null);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void changeUsername_shouldReturn404_whenResourceNotFound() {
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changeUsername(request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void changeUsername_shouldReturn409_whenPasswordMismatch() {
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        when(authService.changeUsername(request)).thenThrow(PasswordMismatchException.class);
        restTestClient.put()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.USERNAME))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

}
