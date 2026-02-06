package com.keldorn.phenylalaninecalculatorapi.repository;

import com.keldorn.phenylalaninecalculatorapi.annotation.MySQLRepositoryTest;
import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

@MySQLRepositoryTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private final String NON_EXISTENT_VALUE = " non-existent";

    @BeforeEach
    public void setUp() {
        userRepository.save(TestEntityFactory.user());
    }

    @Test
    public void UserRepository_SaveDuplicateUsername_ThrowsDataIntegrityViolation() {
        User user = TestEntityFactory.user();
        user.setEmail(NON_EXISTENT_VALUE);
        Assertions.assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void UserRepository_SaveDuplicateEmail_ThrowsDataIntegrityViolation() {
        User user = TestEntityFactory.user();
        user.setUsername(NON_EXISTENT_VALUE);
        Assertions.assertThatThrownBy(() -> userRepository.saveAndFlush(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void UserRepository_FindByUsername_ReturnUser() {
        Optional<User> user = userRepository.findByUsername(TestEntityFactory.DEFAULT_USERNAME);

        Assertions.assertThat(user).isPresent();
        Assertions.assertThat(user.get().getUsername()).isEqualTo(TestEntityFactory.DEFAULT_USERNAME);
    }

    @Test
    public void UserRepository_FindByUsername_ReturnEmptyOptional() {
        Optional<User> user = userRepository.findByUsername(NON_EXISTENT_VALUE);

        Assertions.assertThat(user).isEmpty();
    }

    @Test
    public void UserRepository_ExistsByEmail_ReturnTrue() {
        Boolean response = userRepository.existsByEmail(TestEntityFactory.DEFAULT_EMAIL);

        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void UserRepository_ExistsByEmail_ReturnFalse() {
        Boolean response = userRepository.existsByEmail(NON_EXISTENT_VALUE);

        Assertions.assertThat(response).isFalse();
    }

    @Test
    public void UserRepository_ExistsByUsername_ReturnTrue() {
        Boolean response = userRepository.existsByUsername(TestEntityFactory.DEFAULT_USERNAME);

        Assertions.assertThat(response).isTrue();
    }

    @Test
    public void UserRepository_ExistsByUsername_ReturnFalse() {
        Boolean response = userRepository.existsByUsername(NON_EXISTENT_VALUE);

        Assertions.assertThat(response).isFalse();
    }
}
