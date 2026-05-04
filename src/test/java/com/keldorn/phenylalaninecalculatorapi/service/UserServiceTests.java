package com.keldorn.phenylalaninecalculatorapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.DeletedUserTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

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

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeleteUserAssociationsService deleteUserAssociationsService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(TestEntityFactory.DEFAULT_USERNAME, null,
                        Collections.emptyList());
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
        doAssertionsCheckOnResponse(response, user);
    }

    @Test
    public void getProfile_shouldThrowDeletedUserTokenReceivedException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(DeletedUserTokenReceivedException.class);
    }

    @Test
    public void getProfile_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();
        Assertions.assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
    }

    @Test
    public void update_shouldReturnUserResponse() {
        UserRequest request = new UserRequest("New Email", BigDecimal.ONE);
        User user = TestEntityFactory.user();
        User expectedUser = TestEntityFactory.user();
        expectedUser.setEmail(request.email());
        expectedUser.setDailyLimit(request.dailyLimit());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.update(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        Assertions.assertThat(savedUser.getEmail()).isEqualTo(request.email());
        Assertions.assertThat(savedUser.getDailyLimit()).isEqualByComparingTo(request.dailyLimit());
        doAssertionsCheckOnResponse(response, expectedUser);
    }

    @Test
    public void update_shouldThrowDeletedUserTokenReceivedException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest(null, null)))
                .isInstanceOf(DeletedUserTokenReceivedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowInvalidJwtTokenReceivedException_whenAuthenticationMissing() {
        SecurityContextHolder.clearContext();
        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest(null, null)))
                .isInstanceOf(InvalidJwtTokenReceivedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowEmailIsTakenException_whenEmailIsTaken() {
        User user = TestEntityFactory.user();
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);
        Assertions.assertThatThrownBy(() -> userService.update(new UserRequest("testEmail", null)))
                .isInstanceOf(EmailIsTakenException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void delete_whenUserExists() {
        User user = TestEntityFactory.user();
        user.setUserId(1L);
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        userService.delete();
        verify(deleteUserAssociationsService).removeAssociation(user.getUserId());
        verify(userRepository).deleteById(user.getUserId());
    }

    @Test
    public void delete_shouldThrowDeletedUserTokenReceivedException() {
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> userService.delete())
                .isInstanceOf(DeletedUserTokenReceivedException.class);
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
        Assertions.assertThat(response.email()).isEqualTo(user.getEmail());
    }

}
