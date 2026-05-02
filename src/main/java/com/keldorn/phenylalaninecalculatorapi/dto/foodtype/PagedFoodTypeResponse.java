package com.keldorn.phenylalaninecalculatorapi.dto.foodtype;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.keldorn.phenylalaninecalculatorapi.dto.page.PageResponse;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
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
public class PagedFoodTypeResponse {

    @Valid
    @Builder.Default
    @JsonProperty("content")
    @Schema(name = "content", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid FoodTypeResponse> content = new ArrayList<>();

    @Valid
    @NotNull
    @JsonProperty("page")
    @Schema(name = "page", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResponse page;

}
