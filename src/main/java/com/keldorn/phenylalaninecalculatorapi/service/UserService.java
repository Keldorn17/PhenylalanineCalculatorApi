package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.config.RequestAttributes;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.EmailIsTakenException;
import com.keldorn.phenylalaninecalculatorapi.exception.DeletedUserTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidJwtTokenReceivedException;
import com.keldorn.phenylalaninecalculatorapi.mapper.UserMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

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

    private final HttpServletRequest request;
    private final UserRepository userRepository;
    private final DeleteUserAssociationsService deleteUserAssociationsService;

    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        User user = getCurrentUser();
        log.debug("Getting Profile information for: {}", user.getUserId());
        return UserMapper.INSTANCE.toModel(user);
    }

    @Transactional
    public UserResponse update(UserRequest request) {
        var user = getCurrentUser();
        log.debug("Updating user information for: {}", user.getUserId());
        if (request.email() != null) {
            isEmailTakenAndThrow(request.email());
        }
        UserMapper.INSTANCE.updateEntity(request, user);
        return UserMapper.INSTANCE.toModel(userRepository.save(user));
    }

    @Transactional
    public void delete() {
        Long userId = getCurrentUserId();
        log.debug("Deleting user for: {}", userId);
        deleteUserAssociationsService.removeAssociation(userId);
        userRepository.deleteById(userId);
    }

    protected User getCurrentUser() {
        log.debug("Getting current user");
        User cachedUser = (User) request.getAttribute(RequestAttributes.CURRENT_USER);
        if (cachedUser != null) {
            log.debug("Found user in request cache");
            return cachedUser;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {
            log.debug("Found user in SecurityContext principal");
            request.setAttribute(RequestAttributes.CURRENT_USER, user);
            return user;
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidJwtTokenReceivedException("No authentication found");
        }
        log.debug("Fetching user from database for username: {}", authentication.getName());
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new DeletedUserTokenReceivedException("Unauthorized"));
        request.setAttribute(RequestAttributes.CURRENT_USER, user);
        return user;
    }

    protected Long getCurrentUserId() {
        log.debug("Getting current user's id");
        Long userId = (Long) request.getAttribute(RequestAttributes.CURRENT_USER_ID);
        if (userId != null) {
            return userId;
        }
        return getCurrentUser().getUserId();
    }

    protected User getCurrentUserReference() {
        log.debug("Getting current user reference");
        return userRepository.getReferenceById(getCurrentUserId());
    }

    protected void isEmailTakenAndThrow(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailIsTakenException("Email is taken");
        }
    }

}
