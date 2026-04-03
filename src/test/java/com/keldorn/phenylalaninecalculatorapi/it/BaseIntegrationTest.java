package com.keldorn.phenylalaninecalculatorapi.it;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiPaths;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.utils.RestTestUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.mysql.MySQLContainer;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest extends RestTestUtils {

    @Autowired
    private RestTestClient restTestClient;

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

    protected AuthResponse registerTestUser() {
        return restTestClient.post()
                .uri(path(ApiRoutes.AUTH_PATH, ApiPaths.REGISTER))
                .body(new AuthRegisterRequest(
                        TestEntityFactory.DEFAULT_EMAIL,
                        TestEntityFactory.DEFAULT_USERNAME,
                        TestEntityFactory.DEFAULT_PASSWORD,
                        TestEntityFactory.DEFAULT_TIMEZONE
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void doAssertionChecksOnResponse(ErrorResponse response, ErrorResponse expectedResponse) {
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getType()).isEqualTo(expectedResponse.getType());
        Assertions.assertThat(response.getTitle()).isEqualTo(expectedResponse.getTitle());
        Assertions.assertThat(response.getDetails()).contains(expectedResponse.getDetails());
        Assertions.assertThat(response.getStatusCode()).isEqualTo(expectedResponse.getStatusCode());
    }

    protected void doAssertionChecksOnResponse(RestTestClient.ResponseSpec responseSpec,
            ErrorResponse expectedResponse) {
        ErrorResponse response = responseSpec.expectBody(ErrorResponse.class).returnResult().getResponseBody();
        doAssertionChecksOnResponse(response, expectedResponse);
    }

}
