package com.keldorn.phenylalaninecalculatorapi.it;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

public class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    void register_shouldReturn200() {
        AuthResponse response = registerTestUser();
        Assertions.assertThat(response).isNotNull();
    }

    @Test
    void register_shouldReturn400_whenRequiredDataMissing() {
        AuthRegisterRequest request = new AuthRegisterRequest(null, null, null, null);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("register")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void register_shouldReturn409_whenEmailIsTaken() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                "Not Taken Username",
                TestEntityFactory.DEFAULT_PASSWORD,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        registerTestUser();
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("register")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_shouldReturn409_whenUsernameIsTaken() {
        AuthRegisterRequest request = new AuthRegisterRequest(
                "not_taken_email@gmail.com",
                TestEntityFactory.DEFAULT_USERNAME,
                TestEntityFactory.DEFAULT_PASSWORD,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        registerTestUser();
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("register")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void authenticate_shouldReturn200() {
        registerTestUser();
        AuthRequest request = new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        AuthResponse response = restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("authenticate")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
    }

    @Test
    void authenticate_shouldReturn400_whenRequiredDataMissing() {
        registerTestUser();
        AuthRequest request = new AuthRequest(null, null);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("authenticate")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticate_shouldReturn401_whenUnauthorized() {
        AuthRequest request = new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("authenticate")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changePassword_shouldReturn200() {
        String token = registerTestUser().token();
        String newPassword = "newPassword";
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, newPassword);
        AuthRequest authRequest = new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, newPassword);
        AuthResponse response = restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("authenticate")
                        .build()
                )
                .body(authRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void changePassword_shouldReturn400_whenRequiredDataMissing() {
        String token = registerTestUser().token();
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest(null, null);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void changePassword_shouldReturn401_whenUnauthorized() {
        String newPassword = "newPassword";
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, newPassword);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changePassword_shouldReturn401_whenZombieTokenReceived() {
        String token = registerTestUser().token();
        String newPassword = "newPassword";
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest(TestEntityFactory.DEFAULT_PASSWORD, newPassword);
        userRepository.deleteAll();
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changePassword_shouldReturn409_whenOldPasswordIsEqualToNewPassword() {
        String token = registerTestUser().token();
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest("oldPassword", "oldPassword");
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changePassword_shouldReturn409_whenBadCredentials() {
        String token = registerTestUser().token();
        AuthPasswordChangeRequest request =
                new AuthPasswordChangeRequest("invalidPassword", TestEntityFactory.DEFAULT_PASSWORD);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("password")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changeUsername_shouldReturn200() {
        String token = registerTestUser().token();
        String newUsername = "newUsername";
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(newUsername, TestEntityFactory.DEFAULT_PASSWORD);
        AuthRequest authRequest = new AuthRequest(newUsername, TestEntityFactory.DEFAULT_PASSWORD);
        AuthResponse response = restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(response).isNotNull();
        restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("authenticate")
                        .build()
                )
                .body(authRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void changeUsername_shouldReturn400_whenRequiredDataMissing() {
        String token = registerTestUser().token();
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(null, null);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void changeUsername_shouldReturn401_whenUnauthorized() {
        String newUsername = "newUsername";
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(newUsername, TestEntityFactory.DEFAULT_PASSWORD);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changUsername_shouldReturn401_whenZombieTokenReceived() {
        String token = registerTestUser().token();
        String newUsername = "newUsername";
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(newUsername, TestEntityFactory.DEFAULT_PASSWORD);
        userRepository.deleteAll();
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void changeUsername_shouldReturn409_whenUsernameIsTaken() {
        String token = registerTestUser().token();
        AuthUsernameChangeRequest request =
                new AuthUsernameChangeRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD);
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changeUsername_shouldReturn409_whenBadCredentials() {
        String token = registerTestUser().token();
        String newUsername = "newUsername";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, "Invalid Password");
        restTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("username")
                        .build()
                )
                .headers(authorizationHeader(token))
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

}
