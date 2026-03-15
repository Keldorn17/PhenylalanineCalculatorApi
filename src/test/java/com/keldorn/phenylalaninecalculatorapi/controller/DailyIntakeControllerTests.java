package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.DailyIntakeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.DailyIntakeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@WebMvcTest(DailyIntakeController.class)
public class DailyIntakeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyIntakeService dailyIntakeService;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void getDailyIntake_shouldReturn200AndDailyIntake() {
        DailyIntakeResponse expectedResponse = new DailyIntakeResponse(1L, TestEntityFactory.TEST_DATE, BigDecimal.TEN);

        when(dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE)).thenReturn(expectedResponse);

        DailyIntakeResponse response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/daily-intake")
                        .queryParam("date", TestEntityFactory.TEST_DATE)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(DailyIntakeResponse.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.id()).isEqualTo(expectedResponse.id());
        Assertions.assertThat(response.date()).isEqualTo(expectedResponse.date());
        Assertions.assertThat(response.totalPhenylalanine()).isEqualByComparingTo(expectedResponse.totalPhenylalanine());
    }

    @Test
    void getDailyIntake_shouldReturn400_whenDateIsMalformed() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/daily-intake")
                        .queryParam("date", "invalid")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getDailyIntake_shouldReturn404_whenDataNotFoundByDate() {
        when(dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE))
                .thenThrow(DailyIntakeNotFoundException.class);

        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/daily-intake")
                        .queryParam("date", TestEntityFactory.TEST_DATE)
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }
}
