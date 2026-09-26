package com.keldorn.phenylalaninecalculatorapi.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(source);
        } catch (DateTimeParseException _) {
            try {
                return LocalDate.parse(source).atStartOfDay(ZoneOffset.UTC);
            } catch (DateTimeParseException _) {
                return LocalDateTime.parse(source).atZone(ZoneOffset.UTC);
            }
        }
    }

}
