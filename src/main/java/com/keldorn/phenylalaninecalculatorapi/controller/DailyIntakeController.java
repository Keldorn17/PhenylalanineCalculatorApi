package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.annotation.BadRequestApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ConflictApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.ForbiddenApiResponse;
import com.keldorn.phenylalaninecalculatorapi.annotation.NotFoundApiResponse;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerDescriptions;
import com.keldorn.phenylalaninecalculatorapi.constant.SwaggerResponseCodes;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.service.DailyIntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiRoutes.DAILY_INTAKE_PATH)
@Tag(name = "Daily Intake", description = "Endpoint for showing the user's daily intake")
public class DailyIntakeController {

    private final DailyIntakeService dailyIntakeService;

    @Operation(
            summary = "Retrieves a daily intake entry by date",
            responses = {
                    @ApiResponse(
                            responseCode = SwaggerResponseCodes.OK,
                            description = SwaggerDescriptions.SUCCESS_GET,
                            content = @Content(schema = @Schema(implementation = DailyIntakeResponse.class))
                    )
            }
    )
    @BadRequestApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @GetMapping
    public ResponseEntity<DailyIntakeResponse> getDailyIntake(
            @Parameter(description = "Date of intake (ISO-8601)", example = "2026-01-01")
            @RequestParam LocalDate date
    ) {
        log.info("Get request: {}", ApiRoutes.DAILY_INTAKE_PATH);
        return ResponseEntity.ok(dailyIntakeService.findByDate(date));
    }
}
