package com.keldorn.phenylalaninecalculatorapi.it;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.mysql.MySQLContainer;

@DirtyTest
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

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
        AuthRegisterRequest request = new AuthRegisterRequest(
                TestEntityFactory.DEFAULT_EMAIL,
                TestEntityFactory.DEFAULT_USERNAME,
                TestEntityFactory.DEFAULT_PASSWORD,
                TestEntityFactory.DEFAULT_TIMEZONE
        );
        return restTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiRoutes.AUTH_PATH)
                        .pathSegment("register")
                        .build()
                )
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
    }

    protected Consumer<HttpHeaders> authorizationHeader(String token) {
        return httpHeaders -> httpHeaders.add("Authorization", "Bearer " + token);
    }

}
