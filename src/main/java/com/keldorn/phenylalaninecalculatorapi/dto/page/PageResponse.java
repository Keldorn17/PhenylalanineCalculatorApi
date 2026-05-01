package com.keldorn.phenylalaninecalculatorapi.dto.page;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {

    @NotNull
    @JsonProperty("size")
    @Schema(name = "size", example = "10", description = "The number of elements returned in the response",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer size;

    @NotNull
    @JsonProperty("number")
    @Schema(name = "number", example = "0", description = "Current number of the page returned in the response",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer number;

    @NotNull
    @JsonProperty("totalElements")
    @Schema(name = "totalElements", example = "20", description = "Number of total elements", requiredMode =
            Schema.RequiredMode.REQUIRED)
    private Integer totalElements;

    @NotNull
    @JsonProperty("totalPages")
    @Schema(name = "totalPages", example = "2", description = "Number of total pages", requiredMode =
            Schema.RequiredMode.REQUIRED)
    private Integer totalPages;

}
