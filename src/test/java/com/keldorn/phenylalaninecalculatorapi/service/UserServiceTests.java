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
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

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
        User user = TestEntityFactory.user();
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile();

        verify(userMapper).toResponse(user);

        doAssertionsCheckOnResponse(response, user);
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
        User expectedUser = TestEntityFactory.user();
        expectedUser.setEmail(request.email());
        expectedUser.setDailyLimit(request.dailyLimit());
        expectedUser.setTimezone(request.timezone());

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(userMapper).toResponse(any(User.class));
        User savedUser = captor.getValue();

        Assertions.assertThat(savedUser.getEmail()).isEqualTo(request.email());
        Assertions.assertThat(savedUser.getDailyLimit()).isEqualByComparingTo(request.dailyLimit());
        Assertions.assertThat(savedUser.getTimezone()).isEqualTo(request.timezone());
        doAssertionsCheckOnResponse(response, expectedUser);
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
        User expectedUser = TestEntityFactory.user();
        expectedUser.setTimezone("UTC");

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(userMapper).toResponse(any(User.class));
        User savedUser = captor.getValue();

        Assertions.assertThat(savedUser.getTimezone()).isEqualTo(expectedUser.getTimezone());
        doAssertionsCheckOnResponse(response, expectedUser);
    }

    @Test
    public void delete_whenUserExists() {
        User user = TestEntityFactory.user();
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));

        userService.delete();

        verify(userRepository).delete(user);
    }

    @Test
    public void delete_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.delete())
                        .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    public void delete_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        Assertions.assertThatThrownBy(() -> userService.delete())
                .isInstanceOf(InvalidJwtTokenReceivedException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    private void doAssertionsCheckOnResponse(UserResponse response, User user) {
        Assertions.assertThat(response.id()).isEqualTo(user.getUserId());
        Assertions.assertThat(response.username()).isEqualTo(user.getUsername());
        Assertions.assertThat(response.dailyLimit()).isEqualByComparingTo(user.getDailyLimit());
        Assertions.assertThat(response.timezone()).isEqualTo(user.getTimezone());
        Assertions.assertThat(response.email()).isEqualTo(user.getEmail());
    }
}
