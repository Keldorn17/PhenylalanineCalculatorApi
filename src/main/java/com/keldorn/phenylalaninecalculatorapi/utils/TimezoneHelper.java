package com.keldorn.phenylalaninecalculatorapi.utils;

import java.time.ZoneId;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimezoneHelper {

    private static final Set<String> AVAILABLE_ZONE_IDS = ZoneId.getAvailableZoneIds();

    public static ZoneId resolveZoneId(String timezone) {
        if (isNotValidTimezone(timezone)) {
            return ZoneId.of("UTC");
        }
        return ZoneId.of(timezone);
    }

    private static boolean isNotValidTimezone(String timezoneId) {
        return timezoneId == null || timezoneId.isEmpty() || !AVAILABLE_ZONE_IDS.contains(timezoneId);
    }

}
