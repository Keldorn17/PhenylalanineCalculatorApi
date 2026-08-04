package com.keldorn.phenylalaninecalculatorapi.filter;

import com.keldorn.phenylalaninecalculatorapi.constant.RequestAttributes;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.service.JwtService;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import io.jsonwebtoken.Claims;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthFilter(JwtService jwtService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getToken(request, response, filterChain);
            if (token != null) {
                Claims claims = jwtService.validateAndParseAccessToken(token);
                String username = jwtService.extractUsername(claims);
                Long userId = jwtService.extractUserId(claims);
                request.setAttribute(RequestAttributes.CURRENT_USER_ID, userId);
                authenticate(request, username, claims);
                filterChain.doFilter(request, response);
            }
        } catch (InvalidJwtTokenReceivedException e) {
            resolver.resolveException(request, response, null, e);
        }
    }

    private void authenticate(HttpServletRequest request, String username, Claims claims) {
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var roles = jwtService.extractRoles(claims);
            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private String getToken(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return null;
        }
        return authHeader.substring(7);
    }

}
