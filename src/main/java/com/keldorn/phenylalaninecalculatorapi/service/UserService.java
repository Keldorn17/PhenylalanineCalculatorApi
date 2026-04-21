package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.conflict.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.unauthorized.DeletedUserTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.unauthorized.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.mapper.UserMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeleteUserAssociationsService deleteUserAssociationsService;

    protected User getCurrentUser() {
        log.debug("Getting current user from SecurityContextHolder");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidJwtTokenReceivedException("No authentication found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DeletedUserTokenReceivedException("Unauthorized"));
    }

    protected Long getCurrentUserId() {
        log.debug("Getting current user's id");
        return getCurrentUser().getUserId();
    }

    protected void isEmailTakenAndThrow(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailIsTakenException("Email is taken");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        User user = getCurrentUser();
        log.debug("Getting Profile information for: {}", user.getUserId());
        return UserMapper.INSTANCE.toResponse(user);
    }

    @Transactional
    public UserResponse update(UserRequest request) {
        var user = getCurrentUser();
        log.debug("Updating user information for: {}", user.getUserId());
        if (request.email() != null) {
            isEmailTakenAndThrow(request.email());
        }
        UserMapper.INSTANCE.updateEntity(request, user);
        return UserMapper.INSTANCE.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete() {
        User user = getCurrentUser();
        Long userId = user.getUserId();
        log.debug("Deleting user for: {}", userId);
        deleteUserAssociationsService.removeAssociation(userId);
        userRepository.delete(user);
    }

}
