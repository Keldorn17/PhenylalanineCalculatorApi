package com.keldorn.phenylalaninecalculatorapi.converter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class StringToZonedDateTimeConverterTests {

    private final StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();

    @Test
    void convert_shouldReturnNull_whenSourceIsNull() {
        Assertions.assertThat(converter.convert(null)).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenSourceIsBlank() {
        Assertions.assertThat(converter.convert("   ")).isNull();
    }

    @Test
    void convert_shouldParseIsoLocalDate() {
        ZonedDateTime result = converter.convert("2026-01-01");
        Assertions.assertThat(result).isEqualTo(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneOffset.UTC));
    }

    @Test
    void convert_shouldParseIsoZonedDateTime() {
        ZonedDateTime result = converter.convert("2026-01-01T15:30:00Z");
        Assertions.assertThat(result).isEqualTo(ZonedDateTime.of(2026, 1, 1, 15, 30, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void convert_shouldParseIsoZonedDateTimeWithOffset() {
        ZonedDateTime result = converter.convert("2026-06-01T23:45:00+02:00");
        Assertions.assertThat(result).isEqualTo(ZonedDateTime.parse("2026-06-01T23:45:00+02:00"));
    }

    @Test
    void convert_shouldParseLocalDateTime() {
        ZonedDateTime result = converter.convert("2026-01-01T12:00:00");
        Assertions.assertThat(result).isEqualTo(ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void convert_shouldThrowDateTimeParseException_whenMalformed() {
        Assertions.assertThatThrownBy(() -> converter.convert("malformed date"))
                .isInstanceOf(DateTimeParseException.class);
    }

}
