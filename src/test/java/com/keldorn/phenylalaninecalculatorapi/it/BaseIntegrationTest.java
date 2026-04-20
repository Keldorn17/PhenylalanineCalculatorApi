package com.keldorn.phenylalaninecalculatorapi.it;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiPaths;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.utils.RestTestUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.mysql.MySQLContainer;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest extends RestTestUtils {

    @Autowired
    protected RestTestClient restTestClient;

    private String cachedToken;

    static final MySQLContainer mysql = new MySQLContainer("mysql:9.6.0")
            .withDatabaseName("phenylalanine")
            .withUsername("test_user")
            .withPassword("test_pass");

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    protected String getAuthToken() {
        if (cachedToken == null) {
            AuthResponse response = restTestClient.post()
                    .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.AUTHENTICATE))
                    .body(new AuthRequest(TestEntityFactory.DEFAULT_USERNAME, TestEntityFactory.DEFAULT_PASSWORD))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthResponse.class)
                    .returnResult()
                    .getResponseBody();
            Assertions.assertThat(response).isNotNull();
            cachedToken = response.token();
        }
        return cachedToken;
    }

    protected static ErrorResponse error(HttpStatus status, String details) {
        return new ErrorResponse(
                ApiResponses.CLIENT_ERROR,
                status.getReasonPhrase(),
                details,
                status
        );
    }

    protected void verifyError(RestTestClient.ResponseSpec spec, ErrorResponse expected) {
        if (expected != null) {
            spec.expectBody(ErrorResponse.class)
                    .value(actual -> Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected));
        }
    }

}
