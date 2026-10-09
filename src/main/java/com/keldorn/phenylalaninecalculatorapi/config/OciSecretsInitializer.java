package com.keldorn.phenylalaninecalculatorapi.config;

import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

public class OciSecretsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var env = context.getEnvironment();
        if (!Arrays.asList(env.getActiveProfiles()).contains("oci")) {
            return;
        }
        String secretOcid = env.getProperty("OCI_SECRET_BUNDLE_OCID");
        if (secretOcid == null || secretOcid.isBlank()) {
            throw new IllegalStateException("OCI_SECRET_BUNDLE_OCID environment variable is missing for profile 'oci'");
        }
        var provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        try (SecretsClient client = SecretsClient.builder().build(provider)) {
            var request = GetSecretBundleRequest.builder()
                    .secretId(secretOcid)
                    .stage(GetSecretBundleRequest.Stage.Current)
                    .build();
            GetSecretBundleResponse response = client.getSecretBundle(request);
            var content = (Base64SecretBundleContentDetails) response.getSecretBundle().getSecretBundleContent();
            String decodedProperties =
                    new String(Base64.getDecoder().decode(content.getContent()), StandardCharsets.UTF_8);
            Properties props = new Properties();
            props.load(new StringReader(decodedProperties));
            Map<String, Object> propertyMap = new HashMap<>();
            for (String name : props.stringPropertyNames()) {
                propertyMap.put(name, props.getProperty(name));
            }
            env.getPropertySources().addFirst(new MapPropertySource("ociVaultBundle", propertyMap));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load secrets bundle from OCI Vault", e);
        }
    }

}
