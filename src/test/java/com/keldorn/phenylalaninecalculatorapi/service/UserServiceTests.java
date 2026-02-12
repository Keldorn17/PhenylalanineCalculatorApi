package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.UserNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.exception.unauthorized.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.mapper.UserMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(TestEntityFactory.DEFAULT_USERNAME, null, Collections.emptyList());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    public void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getProfile_shouldReturnUserResponse_whenUserExists() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(TestEntityFactory.user()));
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse(1L, null, null, null, null));

        UserResponse response = userService.getProfile();

        Assertions.assertThat(response).isNotNull();
    }

    @Test
    public void getProfile_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    public void getProfile_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        Assertions.assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
    }

    @Test
    public void update_shouldReturnUserResponse() {
        UserRequest request = new UserRequest("New Email", BigDecimal.ONE, "UTC");
        User user = TestEntityFactory.user();

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(null);

        userService.update(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getEmail()).isEqualTo(request.email());
        Assertions.assertThat(savedUser.getDailyLimit()).isEqualByComparingTo(request.dailyLimit());
        Assertions.assertThat(savedUser.getTimezone()).isEqualTo(request.timezone());
    }

    @Test
    public void update_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest(null, null, null)))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest(null, null, null)))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowEmailIsTakenException_whenEmailIsTaken() {
        User user = TestEntityFactory.user();

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest("testEmail", null, null)))
                .isInstanceOf(EmailIsTakenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    public void update_shouldReturnUserResponseWithUTCTimezone_whenUserSetInvalidTimezone() {
        UserRequest request = new UserRequest(null, null, "Invalid Timezone");
        User user = TestEntityFactory.user();

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(null);

        userService.update(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getTimezone()).isEqualTo("UTC");
    }

    @Test
    public void delete_whenUserExists() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(TestEntityFactory.user()));

        userService.delete();

        verify(userRepository).delete(any(User.class));
    }

    @Test
    public void delete_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.delete())
                        .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    public void delete_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        Assertions.assertThatThrownBy(() -> userService.delete())
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

        verify(userRepository, never()).delete(any(User.class));
    }
}
