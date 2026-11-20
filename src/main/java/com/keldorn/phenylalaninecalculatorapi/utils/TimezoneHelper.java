package com.keldorn.phenylalaninecalculatorapi.utils;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class TimezoneHelper {

    public static ZoneId resolveZoneId(String timezone) {
        return resolveZoneId(timezone, ZoneOffset.UTC);
    }

    public static ZoneId resolveZoneId(String timezone, ZoneId fallback) {
        ZoneId defaultZone = fallback != null ? fallback : ZoneOffset.UTC;
        if (timezone == null || timezone.isBlank()) {
            return defaultZone;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException _) {
            log.warn("Invalid timezone '{}', falling back to {}", timezone, defaultZone);
            return defaultZone;
        }
    }

}
