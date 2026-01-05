package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.*;
import com.keldorn.phenylalaninecalculatorapi.exception.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthResponse authenticate(AuthRequest request) {
        log.debug("Authenticating User.");
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        manageAuth(user.getUsername(), request.password());
        return getResponse(user);
    }

    public AuthResponse register(AuthRegisterRequest request) {
        log.debug("Registering New User.");
        isUsernameTakenAndThrow(request.username());
        userService.isEmailTakenAndThrow(request.email());
        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodePassword(request.password()))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        manageAuth(request.username(), request.password());
        return getResponse(user);
    }

    public AuthResponse changePassword(AuthPasswordChangeRequest request) {
        log.debug("Changing users password");
        if (request.oldPassword().equals(request.password())) {
            throw new PasswordMismatchException("Old password can not be equal to new password.");
        }
        var user = userService.getCurrentUser();
        if (checkIfTwoPasswordNotMatch(request.oldPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Invalid password passed as old password.");
        }
        user.setPassword(encodePassword(request.password()));
        userRepository.save(user);
        manageAuth(user.getUsername(), request.password());
        return getResponse(user);
    }

    public AuthResponse changeUsername(AuthUsernameChangeRequest request) {
        log.debug("Change users username");
        isUsernameTakenAndThrow(request.username());
        var user = userService.getCurrentUser();
        if (checkIfTwoPasswordNotMatch(request.password(), user.getPassword())) {
            throw new PasswordMismatchException("Invalid password passed.");
        }
        user.setUsername(request.username());
        userRepository.save(user);
        manageAuth(request.username(), request.password());
        return getResponse(user);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean checkIfTwoPasswordNotMatch(String rawPassword, String encodedPassword) {
        return !passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private AuthResponse getResponse(User user) {
        log.debug("Authentication Succeeded, Sending Token Back.");
        var jwtToken = jwtService.generateToken(user.getUsername());
        return new AuthResponse(jwtToken);
    }

    private void manageAuth(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
    }

    private void isUsernameTakenAndThrow(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameIsTakenException("Username is taken.");
        }
    }
}
