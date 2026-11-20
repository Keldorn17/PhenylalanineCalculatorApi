package com.keldorn.phenylalaninecalculatorapi.utils;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TimezoneHelperTests {

    @Test
    void resolveZoneId_shouldReturnIanaZone_whenValid() {
        ZoneId result = TimezoneHelper.resolveZoneId("Europe/Budapest");
        Assertions.assertThat(result).isEqualTo(ZoneId.of("Europe/Budapest"));
    }

    @Test
    void resolveZoneId_shouldReturnOffsetZone_whenFixedOffset() {
        ZoneId result = TimezoneHelper.resolveZoneId("+01:00");
        Assertions.assertThat(result).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    void resolveZoneId_shouldReturnUtc_whenNullOrBlankOrInvalid() {
        Assertions.assertThat(TimezoneHelper.resolveZoneId(null)).isEqualTo(ZoneOffset.UTC);
        Assertions.assertThat(TimezoneHelper.resolveZoneId("   ")).isEqualTo(ZoneOffset.UTC);
        Assertions.assertThat(TimezoneHelper.resolveZoneId("Invalid/Timezone")).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void resolveZoneId_shouldUseFallback_whenTimezoneIsNullOrInvalid() {
        ZoneId fallback = ZoneOffset.ofHours(2);
        Assertions.assertThat(TimezoneHelper.resolveZoneId(null, fallback)).isEqualTo(fallback);
        Assertions.assertThat(TimezoneHelper.resolveZoneId("Invalid/Timezone", fallback)).isEqualTo(fallback);
        Assertions.assertThat(TimezoneHelper.resolveZoneId("Europe/Budapest", fallback))
                .isEqualTo(ZoneId.of("Europe/Budapest"));
    }

}
