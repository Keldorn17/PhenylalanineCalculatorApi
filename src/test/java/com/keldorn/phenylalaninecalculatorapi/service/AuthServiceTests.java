package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;
import com.keldorn.phenylalaninecalculatorapi.dto.auth.*;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.PasswordMismatchException;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.UsernameIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

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

    private final String RETURN_TOKEN = "Test token";
    private final String ENCODED_PASSWORD = "Encoded Password";

    @Test
    public void authenticate_shouldReturnAuthResponse_whenCorrectCredentialsProvided() {
        User user = TestEntityFactory.user();
        AuthRequest request = new AuthRequest(user.getUsername(), user.getPassword());

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(request.username())).thenReturn(RETURN_TOKEN);

        AuthResponse response = authService.authenticate(request);

        Assertions.assertThat(response.token()).isEqualTo(RETURN_TOKEN);
    }

    @Test
    public void authenticate_shouldThrow_whenUsernameNotFound() {
        AuthRequest request = new AuthRequest("Test", null);

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void authenticate_shouldThrow_whenBadCredentials() {
        User user = TestEntityFactory.user();
        AuthRequest request = new AuthRequest(user.getUsername(), "Invalid Password");

        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any());

        Assertions.assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    public void register_shouldReturnAuthResponse_whenRegistrationSucceeds() {
        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", "password", "UTC");

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(request.username())).thenReturn(RETURN_TOKEN);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        Assertions.assertThat(savedUser.getUsername()).isEqualTo(request.username());
        Assertions.assertThat(savedUser.getTimezone()).isEqualTo(request.timezone());
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
        Assertions.assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
        Assertions.assertThat(response.token()).isEqualTo(RETURN_TOKEN);
    }

    @Test
    public void register_shouldThrow_whenUsernameIsTaken() {
        AuthRegisterRequest request =
                new AuthRegisterRequest(null, "Test", null, null);

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameIsTakenException.class);

        verify(userRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void register_shouldThrow_whenEmailIsTaken() {
        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", null, null);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        doThrow(EmailIsTakenException.class)
                .when(userService)
                .isEmailTakenAndThrow(request.email());

        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailIsTakenException.class);

        verify(userRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void register_shouldThrow_whenBadCredentials() {

        AuthRegisterRequest request =
                new AuthRegisterRequest("test@gmail.com", "Test", "password", "UTC");

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(ENCODED_PASSWORD);
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any());

        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void changePassword_shouldReturnAuthResponse_whenChangeSuccessful() {
        User user = TestEntityFactory.user();
        String newPassword = "New Password";
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest(user.getPassword(), newPassword);

        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.password())).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(user.getUsername())).thenReturn(RETURN_TOKEN);

        AuthResponse response = authService.changePassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        Assertions.assertThat(response.token()).isEqualTo(RETURN_TOKEN);
        Assertions.assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    public void changePassword_shouldThrow_whenChangingPasswordToTheCurrentOne() {
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest("test", "test");

        Assertions.assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void changePassword_shouldThrow_whenOldPasswordIsNotValid() {
        User user = TestEntityFactory.user();
        String newPassword = "New Password";
        AuthPasswordChangeRequest request = new AuthPasswordChangeRequest("invalid old password", newPassword);

        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.oldPassword(), user.getPassword())).thenReturn(false);

        Assertions.assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void changeUsername_shouldReturnAuthResponse_whenChangeSuccessful() {
        User user = TestEntityFactory.user();
        String newUsername = "New Username";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, user.getPassword());

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(request.username())).thenReturn(RETURN_TOKEN);

        AuthResponse response = authService.changeUsername(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        Assertions.assertThat(response.token()).isEqualTo(RETURN_TOKEN);
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    public void changeUsername_shouldThrow_whenUsernameIsTaken() {
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest("Taken Username", null);

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> authService.changeUsername(request))
                .isInstanceOf(UsernameIsTakenException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    public void changeUsername_shouldThrow_whenInvalidPasswordPassed() {
        User user = TestEntityFactory.user();
        String newUsername = "New Username";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, "Invalid password");

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        Assertions.assertThatThrownBy(() -> authService.changeUsername(request))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    // Ideally this shouldn't be happening since password matching should be checked before.
    @Test
    public void changeUsername_shouldThrow_whenBadCredentials() {
        User user = TestEntityFactory.user();
        String newUsername = "New Username";
        AuthUsernameChangeRequest request = new AuthUsernameChangeRequest(newUsername, user.getPassword());

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        doThrow(BadCredentialsException.class)
                .when(authenticationManager)
                .authenticate(any());

        Assertions.assertThatThrownBy(() -> authService.changeUsername(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
