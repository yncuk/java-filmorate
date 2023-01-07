package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.yandex.practicum.filmorate.mapper.CustomMapper.getMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserController userController;
    private final UserDbStorage userStorage;
    private final UserService userService;

    @Order(1)
    @ParameterizedTest(name = "{index}. Check create user with wrong email: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "nickname@", "mail ru"})
    @DisplayName("Check create user with wrong email")
    void createUserWithWrongEmail(String mail) throws Exception {
        // given (ValueSource)
        // when
        User user = User.builder()
                .login("name")
                .email(mail)
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        // then
        mockMvc.perform(post("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(2)
    @ParameterizedTest(name = "{index}. Check create user with empty login or login with space: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "nick name", "mail ru"})
    @DisplayName("Check create user with empty login or login with space")
    void createUserWithEmptyLoginOrWithSpaceLogin(String name) throws Exception {
        // given (ValueSource)
        // when
        User user = User.builder()
                .email("name@mail.ru")
                .login(name)
                .build();
        // then
        mockMvc.perform(post("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(3)
    @Test
    @DisplayName("Check create user with empty name")
    void createUserWithEmptyName() throws EntityNotFoundException {
        // when
        User user = User.builder()
                .email("name@mail.ru")
                .login("name")
                .birthday(LocalDate.of(2000, 12, 20))
                .likedFilms(new HashSet<>(List.of()))
                .build();
        userController.create(user);
        Optional<User> userOptional = Optional.ofNullable(userStorage.findById(2));
        // then
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user2 ->
                        assertThat(user2).hasFieldOrPropertyWithValue("id", 2)
                                .hasFieldOrPropertyWithValue("email", "name@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "name")
                                .hasFieldOrPropertyWithValue("name", "name")
                                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 12, 20))
                                .hasFieldOrPropertyWithValue("likedFilms", new HashSet<>(List.of()))
                );
    }

    @Order(4)
    @Test
    @DisplayName("Check create user with birthday in the future")
    void createUserWithBirthdayInTheFuture() throws Exception {
        // when
        User user = User.builder()
                .email("name@mail.ru")
                .login("name")
                .birthday(LocalDate.of(2030, 12, 20))
                .build();
        // then
        mockMvc.perform(post("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(5)
    @Test
    @DisplayName("Check create user without mistakes")
    void createUserWithoutMistakes() throws EntityNotFoundException {
        // when
        User user = User.builder()
                .email("lastName@mail.ru")
                .login("lastName")
                .birthday(LocalDate.of(2000, 12, 20))
                .name("lastName")
                .likedFilms(new HashSet<>(List.of()))
                .build();
        userController.create(user);
        Optional<User> userOptional = Optional.ofNullable(userStorage.findById(3));
        // then
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user1 ->
                        assertThat(user1).hasFieldOrPropertyWithValue("id", 3)
                                .hasFieldOrPropertyWithValue("email", "lastName@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "lastName")
                                .hasFieldOrPropertyWithValue("name", "lastName")
                                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 12, 20))
                                .hasFieldOrPropertyWithValue("likedFilms", new HashSet<>(List.of()))
                );
    }

    @Order(6)
    @Test
    @DisplayName("Check update unknown user")
    void updateUnknownUser() throws Exception {
        // when
        User user = User.builder()
                .id(10)
                .email("name@mail.ru")
                .login("name")
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        // then
        mockMvc.perform(put("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(7)
    @Test
    @DisplayName("Check update user")
    void updateUser() throws Exception {
        // when
        User user = User.builder()
                .id(3)
                .email("name2@yandex.ru")
                .login("name2")
                .name("name2")
                .birthday(LocalDate.of(2002, 10, 10))
                .build();
        userStorage.update(user);
        // then
        mockMvc.perform(put("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        Optional<User> userOptional = Optional.ofNullable(userStorage.findById(3));
        // then
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user1 ->
                        assertThat(user1).hasFieldOrPropertyWithValue("id", 3).
                                hasFieldOrPropertyWithValue("email", "name2@yandex.ru").
                                hasFieldOrPropertyWithValue("login", "name2").
                                hasFieldOrPropertyWithValue("name", "name2").
                                hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 10))
                );
    }

    @Order(8)
    @Test
    @DisplayName("Get user")
    void getUser() throws Exception {
        // then
        mockMvc.perform(get("/users/3"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("name2@yandex.ru"))
                .andExpect(jsonPath("$.login").value("name2"))
                .andExpect(jsonPath("$.name").value("name2"))
                .andExpect(jsonPath("$.birthday")
                        .value(LocalDate.of(2002, 10, 10).toString()));
    }

    @Order(9)
    @Test
    @DisplayName("Get all users")
    void getAllUsers() throws Exception {
        // then
        mockMvc.perform(get("/users"))
                .andExpect(status().is2xxSuccessful());
    }

    @Order(10)
    @Test
    @DisplayName("User with ID = 2 add friend user with ID = 3")
    void userId2AddFriendUserId3() throws Exception {
        // then
        mockMvc.perform(put("/users/2/friends/3"))
                .andExpect(status().is2xxSuccessful());
        assertTrue(userController.findById(2).getFriends().contains(3L));
    }

    @Order(11)
    @Test
    @DisplayName("User with ID = 4 add friend user with ID = 3")
    void userId4AddFriendUserId3() throws Exception {
        // when
        User user = User.builder()
                .email("third_name@mail.ru")
                .login("third_name")
                .birthday(LocalDate.of(2000, 12, 20))
                .likedFilms(new HashSet<>(List.of()))
                .build();
        userController.create(user);
        // then
        mockMvc.perform(put("/users/4/friends/3"))
                .andExpect(status().is2xxSuccessful());
        assertTrue(userController.findById(4).getFriends().contains(3L));
    }

    @Order(12)
    @Test
    @DisplayName("User with ID = 1 add unknown friend")
    void userId1AddUnknownFriend() throws Exception {
        // then
        mockMvc.perform(put("/users/1/friends/50"))
                .andExpect(status().is4xxClientError());
    }

    @Order(13)
    @Test
    @DisplayName("Check common friend 2 and 4 user id")
    void checkCommonFriendUser2AndUser4() throws Exception {
        // when
        Optional<User> userOptional = Optional.ofNullable(userService.giveMutualFriends(2, 4).get(0));
        // then
        mockMvc.perform(get("/users/2/friends/common/4"))
                .andExpect(status().is2xxSuccessful());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user1 ->
                        assertThat(user1).hasFieldOrPropertyWithValue("id", 3).
                                hasFieldOrPropertyWithValue("email", "name2@yandex.ru").
                                hasFieldOrPropertyWithValue("login", "name2").
                                hasFieldOrPropertyWithValue("name", "name2").
                                hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 10))
                );
    }

    @Order(14)
    @Test
    @DisplayName("User with ID = 2 delete friend with ID = 3 ")
    void userWithId1DeleteFriendWithId2() throws Exception {
        // then
        mockMvc.perform(delete("/users/2/friends/3"))
                .andExpect(status().is2xxSuccessful());
        assertFalse(userController.findById(2).getFriends().contains(3L));
    }

    @Order(15)
    @Test
    @DisplayName("Find all friend user ID = 4")
    void findAllFriendUser4() throws Exception {
        // when
        Optional<User> userOptional = Optional.ofNullable(userStorage.giveFriends(4).get(0));
        // then
        mockMvc.perform(get("/users/4/friends"))
                .andExpect(status().is2xxSuccessful());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user1 ->
                        assertThat(user1).hasFieldOrPropertyWithValue("id", 3).
                                hasFieldOrPropertyWithValue("email", "name2@yandex.ru").
                                hasFieldOrPropertyWithValue("login", "name2").
                                hasFieldOrPropertyWithValue("name", "name2").
                                hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 10))
                );
    }
}