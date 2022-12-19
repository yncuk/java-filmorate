package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.mapper.CustomMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.yandex.practicum.filmorate.mapper.CustomMapper.getMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

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
    void createUserWithEmptyName() {
        // when
        User user = User.builder()
                .email("name@mail.ru")
                .login("name")
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        User user1 = userController.create(user);
        // then
        assertEquals("name", user1.getName());
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
    void createUserWithoutMistakes() {
        // when
        User user = User.builder()
                .email("last_name@mail.ru")
                .login("last_name")
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        User user1 = userController.create(user);
        // then
        assertAll(
                () -> assertEquals(3, user1.getId()),
                () -> assertEquals("last_name@mail.ru", user1.getEmail()),
                () -> assertEquals("last_name", user1.getLogin()),
                () -> assertEquals(LocalDate.of(2000, 12, 20), user1.getBirthday()),
                () -> assertEquals("last_name", user1.getName())
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
        User user = userController.findById(2).withName("name2");
        userController.update(user);
        // then
        mockMvc.perform(put("/users")
                        .content(getMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        User userCurrent = userController.findById(2);
        assertAll(
                () -> assertEquals(2, userCurrent.getId()),
                () -> assertEquals("name@mail.ru", userCurrent.getEmail()),
                () -> assertEquals("name", userCurrent.getLogin()),
                () -> assertEquals(LocalDate.of(2000, 12, 20), userCurrent.getBirthday()),
                () -> assertEquals("name2", userCurrent.getName())
        );
    }

    @Order(8)
    @Test
    @DisplayName("Get user")
    void getUser() throws Exception {
        // then
        mockMvc.perform(get("/users/2"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("name@mail.ru"))
                .andExpect(jsonPath("$.login").value("name"))
                .andExpect(jsonPath("$.name").value("name2"))
                .andExpect(jsonPath("$.birthday")
                        .value(LocalDate.of(2000, 12, 20).toString()));
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
    @DisplayName("User with ID = 1 add friend user with ID = 2")
    void userId1AddFriendUserId2() throws Exception {
        // then
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().is2xxSuccessful());
        assertTrue(userController.findById(1).getFriends().contains(2L));
        assertTrue(userController.findById(2).getFriends().contains(1L));
    }

    @Order(11)
    @Test
    @DisplayName("User with ID = 1 add friend user with ID = 3")
    void userId1AddFriendUserId3() throws Exception {
        // when
        User user = User.builder()
                .email("third_name@mail.ru")
                .login("third_name")
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        userController.create(user);
        // then
        mockMvc.perform(put("/users/1/friends/3"))
                .andExpect(status().is2xxSuccessful());
        assertTrue(userController.findById(1).getFriends().contains(2L));
        assertTrue(userController.findById(1).getFriends().contains(3L));
        assertTrue(userController.findById(2).getFriends().contains(1L));
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
    @DisplayName("Check common friend 2 and 3 user id")
    void checkCommonFriendUser2AndUser3() throws Exception {
        // then
        mockMvc.perform(get("/users/2/friends/common/3"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(CustomMapper.getMapper()
                        .writeValueAsString(List.of(userController.findById(1)))));
    }

    @Order(14)
    @Test
    @DisplayName("User with ID = 1 delete friend with ID = 2 ")
    void userWithId1DeleteFriendWithId2() throws Exception {
        // then
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().is2xxSuccessful());
        assertFalse(userController.findById(1).getFriends().contains(2L));
        assertFalse(userController.findById(2).getFriends().contains(1L));
    }

    @Order(15)
    @Test
    @DisplayName("Find all friend user ID = 1")
    void findAllFriendUser1() throws Exception {
        // then
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(CustomMapper.getMapper()
                        .writeValueAsString(List.of(userController.findById(3)))));
    }
}