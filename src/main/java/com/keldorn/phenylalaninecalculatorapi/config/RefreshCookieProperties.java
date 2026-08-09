package com.keldorn.phenylalaninecalculatorapi.config;

import com.keldorn.phenylalaninecalculatorapi.domain.enums.SameSite;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cookie.refresh")
public class RefreshCookieProperties {

    private boolean httpOnly;
    private boolean secure;
    private SameSite sameSite;

}
