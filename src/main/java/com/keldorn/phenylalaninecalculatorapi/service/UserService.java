package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.UserNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.mapper.UserMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    protected final User getCurrentUser() {
        log.debug("Getting current user from SecurityContextHolder");
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated User Not Found: " + username));
    }

    protected final Long getCurrentUserId() {
        log.debug("Getting current user's id");
        return getCurrentUser().getUserId();
    }

    protected final void isEmailTakenAndThrow(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailIsTakenException("Email is taken");
        }
    }

    public UserResponse getProfile() {
        User user = getCurrentUser();
        log.debug("Getting Profile information for: {}", user.getUserId());
        return userMapper.toResponse(user);
    }

    public UserResponse update(UserRequest request) {
        var user = getCurrentUser();
        log.debug("Updating user information for: {}", user.getUserId());

        if (request.email() != null) {
            isEmailTakenAndThrow(request.email());
            user.setEmail(request.email());
        }
        if (request.dailyLimit() != null) user.setDailyLimit(request.dailyLimit());
        if (request.timezone() != null) user.setTimezone(request.timezone());
        return userMapper.toResponse(userRepository.save(user));
    }

    public void delete() {
        User user = getCurrentUser();
        log.debug("Deleting user for: {}", user.getUserId());
        userRepository.delete(user);
    }
}
