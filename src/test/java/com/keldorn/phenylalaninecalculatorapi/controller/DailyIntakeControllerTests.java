package com.keldorn.phenylalaninecalculatorapi.controller;

import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.ResourceNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.service.DailyIntakeService;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@WebMvcTest(DailyIntakeController.class)
public class DailyIntakeControllerTests {

    @MockitoBean
    private DailyIntakeService dailyIntakeService;

    @Autowired
    private RestTestClient restTestClient;

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
        Assertions.assertThat(response.totalPhenylalanine()).isEqualByComparingTo(
                expectedResponse.totalPhenylalanine());
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
    void getDailyIntake_shouldReturn404_whenResourceNotFound() {
        when(dailyIntakeService.findByDate(TestEntityFactory.TEST_DATE))
                .thenThrow(ResourceNotFoundException.class);
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/daily-intake")
                        .queryParam("date", TestEntityFactory.TEST_DATE)
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

}
