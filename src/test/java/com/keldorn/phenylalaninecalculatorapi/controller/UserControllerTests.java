package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.unauthorized.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.UserService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(UserController.class)
@AutoConfigureRestTestClient
public class UserControllerTests {

    @MockitoBean
    private UserService userService;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void me_shouldReturn200() {
        UserResponse expectedResponse = TestEntityFactory.userResponse();
        when(userService.getProfile()).thenReturn(expectedResponse);
        UserResponse response = restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    @Test
    void me_shouldReturn401_whenUserUnauthorized() {
        when(userService.getProfile()).thenThrow(InvalidJwtTokenReceivedException.class);
        restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void me_shouldReturn404_whenResourceNotFound() {
        when(userService.getProfile()).thenThrow(ResourceNotFoundException.class);
        restTestClient.get()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateUser_shouldReturn200() {
        UserResponse expectedResponse = TestEntityFactory.userResponse();
        UserRequest request = new UserRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        when(userService.update(request)).thenReturn(expectedResponse);
        UserResponse response = restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .returnResult()
                .getResponseBody();
        doAssertionsCheckOnResponse(response, expectedResponse);
    }

    // Skipping actual email validation failure checks, since Spring should have tests for it.
    @Test
    void updateUser_shouldReturn400_whenEmailValidationFails() {
        UserRequest request = new UserRequest(
                "Invalid Email",
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateUser_shouldReturn401_whenUserUnauthorized() {
        UserRequest request = new UserRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        when(userService.update(request)).thenThrow(InvalidJwtTokenReceivedException.class);
        restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void updateUser_shouldReturn404_whenResourceNotFound() {
        UserRequest request = new UserRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_BIG_DECIMAL_VALUE,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        when(userService.update(request)).thenThrow(ResourceNotFoundException.class);
        restTestClient.patch()
                .uri(ApiRoutes.USER_PATH)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteUser_shouldReturn204() {
        restTestClient.delete()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteUser_shouldReturn401_whenUserUnauthorized() {
        doThrow(InvalidJwtTokenReceivedException.class).when(userService).delete();
        restTestClient.delete()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteUser_shouldReturn404_whenResourceNotFound() {
        doThrow(ResourceNotFoundException.class).when(userService).delete();
        restTestClient.delete()
                .uri(ApiRoutes.USER_PATH)
                .exchange()
                .expectStatus().isNotFound();
    }

    private void doAssertionsCheckOnResponse(UserResponse response, UserResponse expectedResponse) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.email()).isEqualTo(expectedResponse.email());
        Assertions.assertThat(response.username()).isEqualTo(expectedResponse.username());
        Assertions.assertThat(response.timezone()).isEqualTo(expectedResponse.timezone());
        Assertions.assertThat(response.dailyLimit()).isEqualByComparingTo(expectedResponse.dailyLimit());
    }

}
