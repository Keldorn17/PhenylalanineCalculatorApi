package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Roles;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponseInternal;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseInternal authenticate(AuthRequest request) {
        log.debug("Authenticating User.");
        User user = manageAuth(request.username(), request.password());
        return getResponse(user);
    }

    @Transactional
    public AuthResponseInternal register(AuthRegisterRequest request) {
        log.debug("Registering New User.");
        userRepository.findByUsernameOrEmail(request.username(), request.email()).ifPresent(u -> {
            if (u.getUsername().equals(request.username())) {
                throw new UsernameIsTakenException("Username is taken.");
            }
            throw new EmailIsTakenException("Email is taken");
        });
        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodePassword(request.password()))
                .roles(List.of(roleService.findByRoleNameOrThrow(Roles.ROLE_USER)))
                .build();
        User savedUser = userRepository.save(user);
        return getResponse(savedUser);
    }

    @Transactional
    public AuthResponseInternal changePassword(AuthPasswordChangeRequest request) {
        log.debug("Changing users password");
        if (request.oldPassword().equals(request.password())) {
            throw new PasswordMismatchException("Old password can not be equal to new password.");
        }
        var user = userService.getCurrentUser();
        if (checkIfTwoPasswordNotMatch(request.oldPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Bad Credentials.");
        }
        user.setPassword(encodePassword(request.password()));
        userRepository.save(user);
        return getResponse(user);
    }

    @Transactional
    public AuthResponseInternal changeUsername(AuthUsernameChangeRequest request) {
        log.debug("Change users username");
        isUsernameTakenAndThrow(request.username());
        var user = userService.getCurrentUser();
        if (checkIfTwoPasswordNotMatch(request.password(), user.getPassword())) {
            throw new PasswordMismatchException("Bad Credentials");
        }
        user.setUsername(request.username());
        userRepository.save(user);
        return getResponse(user);
    }

    public AuthResponseInternal refresh(String refreshToken) {
        log.debug("Refreshing token");
        if (refreshToken == null) {
            throw new InvalidJwtTokenReceivedException("Invalid token received");
        }
        Long userId = jwtService.extractUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidJwtTokenReceivedException("Invalid token received"));
        return refreshTokenService.refresh(refreshToken, user);
    }

    @Transactional
    public void logout(String refreshToken) {
        log.debug("Logging out user");
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean checkIfTwoPasswordNotMatch(String rawPassword, String encodedPassword) {
        return !passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private AuthResponseInternal getResponse(User user) {
        log.debug("Authentication Succeeded, Sending Token Back.");
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = refreshTokenService.save(user);
        return new AuthResponseInternal(accessToken, refreshToken);
    }

    private User manageAuth(String username, String password) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        return (User) authenticate.getPrincipal();
    }

    private void isUsernameTakenAndThrow(String username) {
        boolean existsByUsername = userRepository.existsByUsername(username);
        if (existsByUsername) {
            throw new UsernameIsTakenException("Username is taken.");
        }
    }

}
