package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Иван");
        assertThat(created.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldRejectDuplicateEmailOnCreate() {
        userService.create(userDto("Иван", "ivan@example.com"));

        assertThatThrownBy(() -> userService.create(userDto("Пётр", "ivan@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldReturnUserById() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllUsers() {
        userService.create(userDto("Иван", "ivan@example.com"));
        userService.create(userDto("Пётр", "petr@example.com"));

        Collection<UserDto> users = userService.getAll();

        assertThat(users).extracting(UserDto::getEmail)
                .contains("ivan@example.com", "petr@example.com");
    }

    @Test
    void shouldUpdateNameAndEmail() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        UserDto updated = userService.update(created.getId(), userDto("Иван Петров", "ivan.petrov@example.com"));

        assertThat(updated.getName()).isEqualTo("Иван Петров");
        assertThat(updated.getEmail()).isEqualTo("ivan.petrov@example.com");
    }

    @Test
    void shouldKeepFieldsWhenUpdateValuesAreBlank() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        UserDto updated = userService.update(created.getId(), userDto("   ", "   "));

        assertThat(updated.getName()).isEqualTo("Иван");
        assertThat(updated.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldAllowSameEmailForSameUser() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        UserDto updated = userService.update(created.getId(), userDto("Иван", "ivan@example.com"));

        assertThat(updated.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldRejectEmailTakenByAnotherUser() {
        userService.create(userDto("Иван", "ivan@example.com"));
        UserDto other = userService.create(userDto("Пётр", "petr@example.com"));

        assertThatThrownBy(() -> userService.update(other.getId(), userDto("Пётр", "ivan@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownUser() {
        assertThatThrownBy(() -> userService.update(999L, userDto("Иван", "ivan@example.com")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = userService.create(userDto("Иван", "ivan@example.com"));

        userService.delete(created.getId());

        assertThatThrownBy(() -> userService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenDeletingUnknownUser() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private UserDto userDto(String name, String email) {
        return new UserDto(null, name, email);
    }
}
