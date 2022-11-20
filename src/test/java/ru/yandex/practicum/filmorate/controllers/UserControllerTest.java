package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @ParameterizedTest(name = "{index}. Check create user with empty login or login with space: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "nick name", "mail ru"})
    @DisplayName("Check create user with empty login or login with space")
    void createUserWithEmptyOrWithSpaceLogin(String name) {
        User user = new User();
        user.setEmail("name@mail.ru");
        user.setLogin(name);
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertTrue(thrown.getMessage().contains("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    @DisplayName("Check create user with empty name")
    void createUserWithEmptyName() throws ValidationException {
        User user = new User();
        user.setEmail("name@mail.ru");
        user.setLogin("name");
        user.setBirthday(LocalDate.of(2000, 12, 20));
        User user1 = userController.create(user);
        assertEquals("name", user1.getName());
    }

    @Test
    @DisplayName("Check create user with birthday in the future")
    void createUserWithBirthdayInTheFuture() {
        User user = new User();
        user.setEmail("name@mail.ru");
        user.setLogin("name");
        user.setBirthday(LocalDate.of(2030, 12, 20));
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertTrue(thrown.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("Check create user without mistakes")
    void createUserWithoutMistakes() throws ValidationException {
        User user = new User();
        user.setEmail("name@mail.ru");
        user.setLogin("name");
        user.setBirthday(LocalDate.of(2000, 12, 20));
        User user1 = userController.create(user);
        assertAll(
                () -> assertEquals(user1.getId(), 1),
                () -> assertEquals(user1.getEmail(), "name@mail.ru"),
                () -> assertEquals(user1.getLogin(), "name"),
                () -> assertEquals(user1.getBirthday(), LocalDate.of(2000, 12, 20)),
                () -> assertEquals(user1.getName(), "name")
        );
    }

}