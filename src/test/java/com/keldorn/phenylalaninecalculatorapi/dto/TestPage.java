package com.keldorn.phenylalaninecalculatorapi.dto;

import java.util.List;

public record TestPage<T>(List<T> content, PageMetadata page) {
    public record PageMetadata(int size, int number, int totalElements, int totalPages) {}
}