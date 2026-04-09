package com.keldorn.phenylalaninecalculatorapi.utils;

import java.net.URI;
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

}
