package ru.yandex.practicum.filmorate.controllers;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.CustomMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.yandex.practicum.filmorate.mapper.CustomMapper.getMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FilmController filmController;
    @Autowired
    private UserController userController;

    @Order(1)
    @ParameterizedTest(name = "{index}. Check create film with empty name: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "   ", "    "})
    @DisplayName("Check create film with empty name")
    void createFilmWithEmptyName(String name) throws Exception {
        // when
        Film film = Film.builder()
                .name(name)
                .description("test")
                .releaseDate(LocalDate.of(2000, 10, 12))
                .duration(100)
                .build();
        // then
        mockMvc.perform(
                        post("/films")
                                .content(getMapper().writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(2)
    @Test
    @DisplayName("Check create film with description size more 200")
    void createFilmWithDescriptionMoreThen200() throws Exception {
        // when
        Film film = Film.builder()
                .name("name")
                .description(StringUtils.repeat("test ", 41))
                .build();
        // then
        mockMvc.perform(
                        post("/films")
                                .content(getMapper().writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(3)
    @Test
    @DisplayName("Check create film with release date before movie birthday")
    void createFilmWithReleaseDateBeforeMovieBirthday() throws Exception {
        // when
        Film film = Film.builder()
                .name("name")
                .description("test")
                .releaseDate(LocalDate.of(1800, 10, 12))
                .build();
        // then
        mockMvc.perform(
                        post("/films")
                                .content(getMapper().writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(4)
    @Test
    @DisplayName("Check create film with negative duration")
    void createFilmWithNegativeDuration() throws Exception {
        // when
        Film film = Film.builder()
                .name("name")
                .description("test")
                .releaseDate(LocalDate.of(2000, 10, 12))
                .duration(-100)
                .build();
        // then
        mockMvc.perform(
                        post("/films")
                                .content(getMapper().writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(5)
    @Test
    @DisplayName("Check create film without mistakes")
    void createFilmWithoutMistakes() throws ValidationException {
        // when
        Film film = Film.builder()
                .name("nisi eiusmod")
                .description("adipisicing")
                .releaseDate(LocalDate.of(1967, 3, 25))
                .duration(100)
                .build();
        Film film1 = filmController.create(film);
        // then
        assertAll(
                () -> assertEquals(1, film1.getId()),
                () -> assertEquals("nisi eiusmod", film1.getName()),
                () -> assertEquals("adipisicing", film1.getDescription()),
                () -> assertEquals(LocalDate.of(1967, 3, 25), film1.getReleaseDate()),
                () -> assertEquals(100, film1.getDuration())
        );
    }

    @Order(6)
    @Test
    @DisplayName("Get all films")
    void getAllFilms() throws Exception {
        // when
        mockMvc.perform(get("/films"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(CustomMapper.getMapper()
                        .writeValueAsString(List.of(filmController.findById(1)))));
    }

    @Order(7)
    @Test
    @DisplayName("Get film")
    void getFilm() throws Exception {
        // then
        mockMvc.perform(get("/films/1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("nisi eiusmod"))
                .andExpect(jsonPath("$.description").value("adipisicing"))
                .andExpect(jsonPath("$.releaseDate")
                        .value(LocalDate.of(1967, 3, 25).toString()))
                .andExpect(jsonPath("$.duration").value(100));
    }

    @Order(8)
    @Test
    @DisplayName("Update unknown film")
    void updateUnknownFilm() throws Exception {
        // when
        Film film = Film.builder()
                .id(10)
                .name("nisi eiusmod")
                .description("adipisicing")
                .releaseDate(LocalDate.of(1967, 3, 25))
                .duration(100)
                .build();
        // then
        mockMvc.perform(put("/films")
                        .content(getMapper().writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Order(9)
    @Test
    @DisplayName("Update film")
    void updateFilm() throws Exception {
        // when
        Film film = filmController.findById(1).withLikes(2);
        filmController.update(film);
        // then
        mockMvc.perform(put("/films")
                        .content(getMapper().writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        Film filmCurrent = filmController.findById(1);
        assertAll(
                () -> assertEquals(1, filmCurrent.getId()),
                () -> assertEquals("nisi eiusmod", filmCurrent.getName()),
                () -> assertEquals("adipisicing", filmCurrent.getDescription()),
                () -> assertEquals(LocalDate.of(1967, 3, 25), filmCurrent.getReleaseDate()),
                () -> assertEquals(100, filmCurrent.getDuration()),
                () -> assertEquals(2, filmCurrent.getLikes())
        );
    }

    @Order(10)
    @Test
    @DisplayName("Put like on film with id 2")
    void putLikeOnFilmWithId2() throws Exception {
        // when
        Film film = Film.builder()
                .name("New film")
                .description("New film about friends")
                .releaseDate(LocalDate.of(1999, 4, 30))
                .duration(120)
                .build();
        filmController.create(film);
        User user = User.builder()
                .email("last_name@mail.ru")
                .login("last_name")
                .birthday(LocalDate.of(2000, 12, 20))
                .build();
        userController.create(user);
        // then
        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status().is2xxSuccessful());

        Film filmCurrent = filmController.findById(2);
        User userCurrent = userController.findById(1);
        assertAll(
                () -> assertEquals(2, filmCurrent.getId()),
                () -> assertEquals("New film", filmCurrent.getName()),
                () -> assertEquals("New film about friends", filmCurrent.getDescription()),
                () -> assertEquals(LocalDate.of(1999, 4, 30), filmCurrent.getReleaseDate()),
                () -> assertEquals(120, filmCurrent.getDuration()),
                () -> assertEquals(1, filmCurrent.getLikes())
        );
        assertEquals(new HashSet<>(List.of(2L)), userCurrent.getLikedFilm());
    }

    @Order(11)
    @Test
    @DisplayName("Find popular film")
    void findPopularFilm() throws Exception {
        // then
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(CustomMapper.getMapper()
                        .writeValueAsString(List.of(filmController.findById(1), filmController.findById(2)))));
    }

    @Order(12)
    @Test
    @DisplayName("Find most popular film")
    void findMostPopularFilm() throws Exception {
        // then
        mockMvc.perform(get("/films/popular?count=1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(CustomMapper.getMapper()
                        .writeValueAsString(List.of(filmController.findById(1)))));
    }

    @Order(13)
    @Test
    @DisplayName("Delete like on film with id 2")
    void deleteLikeOnFilmWithId2() throws Exception {
        // then
        mockMvc.perform(delete("/films/2/like/1"))
                .andExpect(status().is2xxSuccessful());

        Film filmCurrent = filmController.findById(2);
        User userCurrent = userController.findById(1);
        assertEquals(0, filmCurrent.getLikes());
        assertEquals(new HashSet<>(), userCurrent.getLikedFilm());
    }
}