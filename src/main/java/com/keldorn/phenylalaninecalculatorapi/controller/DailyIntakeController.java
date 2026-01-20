package com.keldorn.phenylalaninecalculatorapi.controller;

import com.keldorn.phenylalaninecalculatorapi.constant.ApiRoutes;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.service.DailyIntakeService;
import io.swagger.v3.oas.annotations.Operation;
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

    private final String NOT_FOUND = "Daily Intake Found";
    private final String CONFLICT = "Conflict";
    private final String FORBIDDEN = "Forbidden";

    @Operation(operationId = "getDailyIntake",
            summary = "Gets user's daily intake",
            tags = {"Daily Intake"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully found daily intake", content = @Content(schema = @Schema(implementation = DailyIntakeResponse.class))),
                    @ApiResponse(responseCode = "404", description = NOT_FOUND, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = CONFLICT, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = FORBIDDEN, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<DailyIntakeResponse> getDailyIntake(@RequestParam LocalDate date) {
        log.info("Get request: {}", ApiRoutes.DAILY_INTAKE_PATH);
        return ResponseEntity.ok(dailyIntakeService.findByDate(date));
    }
}
