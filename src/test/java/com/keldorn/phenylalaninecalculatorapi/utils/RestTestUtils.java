package com.keldorn.phenylalaninecalculatorapi.utils;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class RestTestUtils {

    protected Consumer<HttpHeaders> withBearer(String token) {
        return httpHeaders -> httpHeaders.setBearerAuth(token);
    }

    protected URI path(String base, String... segments) {
        return UriComponentsBuilder.fromUriString(base)
                .pathSegment(segments)
                .build()
                .toUri();
    }

    protected URI path(String baseById, Long id) {
        return UriComponentsBuilder.fromUriString(baseById)
                .buildAndExpand(id)
                .toUri();
    }

    protected URI path(String base, Integer pageNumber, Integer pageSize, String sort, String query) {
        return UriComponentsBuilder.fromUriString(base)
                .queryParamIfPresent("sort", Optional.ofNullable(sort))
                .queryParamIfPresent("query", Optional.ofNullable(query))
                .queryParamIfPresent("size", Optional.ofNullable(pageSize))
                .queryParamIfPresent("page", Optional.ofNullable(pageNumber))
                .build().toUri();
    }

}
