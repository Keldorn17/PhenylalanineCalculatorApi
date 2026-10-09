package com.keldorn.phenylalaninecalculatorapi.config;

import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("oci")
public class OciVaultConfig {

    @Bean
    public SecretsClient secretsClient() {
        var provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        return SecretsClient.builder().build(provider);
    }

}
