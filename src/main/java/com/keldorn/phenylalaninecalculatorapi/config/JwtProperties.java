package com.keldorn.phenylalaninecalculatorapi.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private Secret secret = new Secret();
    private Access access = new Access();
    private Refresh refresh = new Refresh();

    @Getter
    @Setter
    public static class Secret {
        private String access;
        private String refresh;
    }

    @Getter
    @Setter
    public static class Access {
        private Duration expirationTime;
    }

    @Getter
    @Setter
    public static class Refresh {
        private Duration expirationTime;
    }

}
