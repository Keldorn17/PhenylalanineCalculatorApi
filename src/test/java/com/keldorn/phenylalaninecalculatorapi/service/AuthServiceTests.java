package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthPasswordChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRegisterRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.AuthUsernameChangeRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private final String returnToken = "Test accessToken";
    private final String encodedPassword = "Encoded Password";

    @Test
    void authenticate_shouldReturnAuthResponse_whenCorrectCredentialsProvided() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        AuthRequest request = new AuthRequest(user.getUsername(), user.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(jwtService.generateToken(any(User.class))).thenReturn(returnToken);
        AuthResponse response = authService.authenticate(request);
        Assertions.assertThat(response.accessToken()).isEqualTo(returnToken);
    }

    @Test
    void authenticate_shouldThrow_whenBadCredentials() {
        AuthRequest request = new AuthRequest("user", "Invalid Password");
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any());
        Assertions.assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_shouldReturnAuthResponse_whenRegistrationSucceeds() {
        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", "password");
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        user.setUsername(request.username());
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn(returnToken);
        AuthResponse response = authService.register(request);
        verify(userRepository).save(any(User.class));
        Assertions.assertThat(response.accessToken()).isEqualTo(returnToken);
    }

    @Test
    void register_shouldThrow_whenUsernameIsTaken() {
        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", "password");
        User user = TestEntityFactory.user();
        user.setUsername(request.username());
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameIsTakenException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailIsTaken() {
        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", "password");
        User user = TestEntityFactory.user();
        user.setEmail(request.email());
        user.setUsername("other");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailIsTakenException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void changePassword_shouldReturnAuthResponse_whenChangeSuccessful() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        String newPassword = "New Password";
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest(user.getPassword(), newPassword);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(User.class))).thenReturn(returnToken);
        AuthResponse response = authService.changePassword(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        Assertions.assertThat(response.accessToken()).isEqualTo(returnToken);
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void changePassword_shouldThrow_whenChangingPasswordToTheCurrentOne() {
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest("test", "test");
        Assertions.assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordIsNotValid() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        String newPassword = "New Password";
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest("invalid old password", newPassword);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(false);
        Assertions.assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void changeUsername_shouldReturnAuthResponse_whenChangeSuccessful() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        String newUsername = "New Username";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, user.getPassword());
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn(returnToken);
        AuthResponse response = authService.changeUsername(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        Assertions.assertThat(response.accessToken()).isEqualTo(returnToken);
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void changeUsername_shouldThrow_whenUsernameIsTaken() {
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest("Taken Username", null);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> authService.changeUsername(request))
                .isInstanceOf(UsernameIsTakenException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void changeUsername_shouldThrow_whenInvalidPasswordPassed() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        String newUsername = "New Username";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, "Invalid password");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);
        Assertions.assertThatThrownBy(() -> authService.changeUsername(request))
                .isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }

}
