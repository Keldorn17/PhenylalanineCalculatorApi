package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.annotation.MySQLRepositoryTest;
import com.keldorn.phenylalaninecalculatorapi.annotation.RepositoryCleanUp;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@RepositoryCleanUp
@MySQLRepositoryTest
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private final String nonExistentValue = " non-existent";

    @BeforeEach
    void setUp() {
        userRepository.save(TestEntityFactory.user());
    }

    @Test
    void save_shouldThrowDataIntegrityViolation_whenSavingDuplicateUsername() {
        User user = TestEntityFactory.user();
        user.setEmail(nonExistentValue);
        Assertions.assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldThrowDataIntegrityViolation_whenSavingDuplicateEmail() {
        User user = TestEntityFactory.user();
        user.setUsername(nonExistentValue);
        Assertions.assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByUsername_shouldReturnUser() {
        Optional<User> user = userRepository.findByUsername(TestEntityFactory.DEFAULT_USERNAME);
        Assertions.assertThat(user).isPresent();
        Assertions.assertThat(user.get().getUsername()).isEqualTo(TestEntityFactory.DEFAULT_USERNAME);
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional() {
        Optional<User> user = userRepository.findByUsername(nonExistentValue);
        Assertions.assertThat(user).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        Boolean response = userRepository.existsByEmail(TestEntityFactory.DEFAULT_EMAIL);
        Assertions.assertThat(response).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse() {
        Boolean response = userRepository.existsByEmail(nonExistentValue);
        Assertions.assertThat(response).isFalse();
    }

    @Test
    void existsByUsername_shouldReturnTrue() {
        Boolean response = userRepository.existsByUsername(TestEntityFactory.DEFAULT_USERNAME);
        Assertions.assertThat(response).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse() {
        Boolean response = userRepository.existsByUsername(nonExistentValue);
        Assertions.assertThat(response).isFalse();
    }

}
