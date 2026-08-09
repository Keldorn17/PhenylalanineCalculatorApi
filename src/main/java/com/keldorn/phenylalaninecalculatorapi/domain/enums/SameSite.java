package com.keldorn.phenylalaninecalculatorapi.domain.enums;

import lombok.Getter;

public enum SameSite {
    LAX("Lax"),
    STRICT("Strict"),
    NONE("None");

    @Getter
    private final String value;

    SameSite(String value) {
        this.value = value;
    }
}
