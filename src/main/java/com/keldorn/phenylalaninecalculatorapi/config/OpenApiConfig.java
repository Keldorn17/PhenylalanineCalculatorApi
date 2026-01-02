package com.keldorn.phenylalaninecalculatorapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Phenylalanine calculator API")
                        .description("This API is created in order to help keep track of the daily phenylalanine intake.")
                        .version("v0.0.1")
                );
    }
}
