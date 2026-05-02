package com.keldorn.phenylalaninecalculatorapi.dto.params;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    @Size(max = 500)
    @JsonProperty("query")
    @Schema(name = "query", example = "name=ilike='Some Food' and protein=lt='2'", description = "RSQL query for " +
            "filtering the returned data.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private @Nullable String query;

    @Size(max = 200)
    @Builder.Default
    @JsonProperty("sort")
    @Schema(name = "sort", example = "name,desc", description = "RSQL sort for ordering the returned data.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sort = "name,desc";

}
