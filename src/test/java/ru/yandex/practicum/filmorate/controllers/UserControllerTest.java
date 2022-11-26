package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.mapper.CustomMapper.getMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
    }

    @ParameterizedTest(name = "{index}. Check create user with wrong email: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "nickname@", "mail ru"})
    @DisplayName("Check create user with wrong email")
    void createUserWithWrongEmail(String mail) throws Exception {
        User user = new User();
        user.setLogin("name");
        user.setEmail(mail);
        user.setBirthday(LocalDate.of(2000, 12, 20));
        mockMvc.perform(
                        post("/users")
                                .content(getMapper().writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest(name = "{index}. Check create user with empty login or login with space: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "nick name", "mail ru"})
    @DisplayName("Check create user with empty login or login with space")
    void createUserWithEmptyLoginOrWithSpaceLogin(String name) {
        // given (BeforeEach)
        // when
        User user = new User();
        user.setLogin(name);
        user.setEmail("name@mail.ru");
        // then
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertTrue(thrown.getMessage().contains("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    @DisplayName("Check create user with empty name")
    void createUserWithEmptyName() throws ValidationException {
        // given (BeforeEach)
        // when
        User user = new User();
        user.setLogin("name");
        user.setEmail("name@mail.ru");
        user.setBirthday(LocalDate.of(2000, 12, 20));
        User user1 = userController.create(user);
        // then
        assertEquals("name", user1.getName());
    }

    @Test
    @DisplayName("Check create user with birthday in the future")
    void createUserWithBirthdayInTheFuture() {
        // given (BeforeEach)
        // when
        User user = new User();
        user.setLogin("name");
        user.setEmail("name@mail.ru");
        user.setBirthday(LocalDate.of(2030, 12, 20));
        // then
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertTrue(thrown.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("Check create user without mistakes")
    void createUserWithoutMistakes() throws ValidationException {
        // given (BeforeEach)
        // when
        User user = new User();
        user.setLogin("name");
        user.setEmail("name@mail.ru");
        user.setBirthday(LocalDate.of(2000, 12, 20));
        User user1 = userController.create(user);
        // then
        assertAll(
                () -> assertEquals(user1.getId(), 1),
                () -> assertEquals(user1.getEmail(), "name@mail.ru"),
                () -> assertEquals(user1.getLogin(), "name"),
                () -> assertEquals(user1.getBirthday(), LocalDate.of(2000, 12, 20)),
                () -> assertEquals(user1.getName(), "name")
        );
    }
}