package com.keldorn.phenylalaninecalculatorapi.dto.params;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    @Min(value = 0)
    @Builder.Default
    @JsonProperty("page")
    @Schema(name = "page", example = "1", description = "Controls which page is returned", requiredMode =
            Schema.RequiredMode.NOT_REQUIRED)
    private Integer pageNumber = 0;

    @Builder.Default
    @Min(value = 1)
    @Max(value = 100)
    @JsonProperty("size")
    @Schema(name = "size", example = "20", description = "Controls the number of returned elements", requiredMode =
            Schema.RequiredMode.NOT_REQUIRED)
    private Integer pageSize = 20;

}
