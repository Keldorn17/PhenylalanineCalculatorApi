package com.keldorn.phenylalaninecalculatorapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keldorn.phenylalaninecalculatorapi.constant.ApiResponses;
import com.keldorn.phenylalaninecalculatorapi.dto.error.ErrorResponse;
import com.keldorn.phenylalaninecalculatorapi.security.JwtAuthFilter;
import com.keldorn.phenylalaninecalculatorapi.service.JwtService;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;

    private final String[] freeResourceUrls = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/swagger-resources/**", "/api-docs/**", "/actuator/health", "/api/v1/auth/authenticate",
            "/api/v1/auth/register"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthFilter jwtAuthFilter,
            ObjectMapper objectMapper) {
        log.info("Initializing Spring Security filter chain,");
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(freeResourceUrls).permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((_, response, _) -> {
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .type(ApiResponses.CLIENT_ERROR)
                                    .title(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                    .details(ApiResponses.AUTHENTICATION_REQUIRED_RESPONSE)
                                    .statusCode(HttpStatus.UNAUTHORIZED)
                                    .build();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(), errorResponse);
                        })
                )
                .build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return new JwtAuthFilter(jwtService, userDetailsService, resolver);
    }

}
