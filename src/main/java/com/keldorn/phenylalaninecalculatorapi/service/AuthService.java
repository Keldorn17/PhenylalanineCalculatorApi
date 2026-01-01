package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(AuthRequest request) {
        manageAuth(request.username(), request.password());
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return getResponse(user);
    }

    public AuthResponse register(AuthRegisterRequest request) {
        isUsernameTakenAndThrow(request.username());
        isEmailTakenAndThrow(request.email());
        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        manageAuth(request.username(), request.password());
        return getResponse(user);
    }

    private void isUsernameTakenAndThrow(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameIsTakenException("Username is taken.");
        }
    }

    private void isEmailTakenAndThrow(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailIsTakenException("Email is taken");
        }
    }

    private AuthResponse getResponse(User user) {
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
}
