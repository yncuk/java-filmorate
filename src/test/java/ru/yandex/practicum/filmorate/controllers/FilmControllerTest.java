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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.yandex.practicum.filmorate.mapper.CustomMapper.getMapper;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    FilmController filmController;

    @ParameterizedTest(name = "{index}. Check create film with empty name: \"{arguments}\"")
    @ValueSource(strings = {" ", "  ", "   ", "    "})
    @DisplayName("Check create film with empty name")
    void createFilmWithEmptyName(String name) throws Exception {
        // given (BeforeEach)
        // when
        Film film = Film.builder()
                .name(name)
                .description("test")
                .releaseDate(LocalDate.of(2000, 10, 12))
                .duration(Duration.ofMinutes(100))
                .build();
        // then
        mockMvc.perform(
                        post("/films")
                                .content(getMapper().writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Check create film with description size more 200")
    void createFilmWithDescriptionMoreThen200() {
        // given (BeforeEach)
        // when
        Film film = Film.builder()
                .name("name")
                .description(StringUtils.repeat("test ", 41))
                .build();
        // then
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    @DisplayName("Check create film with release date before movie birthday")
    void createFilmWithReleaseDateBeforeMovieBirthday() {
        // given (BeforeEach)
        // when
        Film film = Film.builder()
                .name("name")
                .description("test")
                .releaseDate(LocalDate.of(1800, 10, 12))
                .build();
        // then
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Дата релиза не может быть раньше дня рождения кино"));
    }

    @Test
    @DisplayName("Check create film with negative duration")
    void createFilmWithNegativeDuration() {
        // given (BeforeEach)
        // when
        Film film = Film.builder()
                .name("name")
                .description("test")
                .releaseDate(LocalDate.of(2000, 10, 12))
                .duration(Duration.ofMinutes(-100))
                .build();
        // then
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Продолжительность фильма должна быть положительной"));
    }

    @Test
    @DisplayName("Check create film without mistakes")
    void createFilmWithoutMistakes() throws ValidationException {
        // given (BeforeEach)
        // when
        Film film = Film.builder()
                .name("nisi eiusmod")
                .description("adipisicing")
                .releaseDate(LocalDate.of(1967, 3, 25))
                .duration(Duration.ofMinutes(100))
                .build();
        Film film1 = filmController.create(film);
        // then
        assertAll(
                () -> assertEquals(film1.getId(), 1),
                () -> assertEquals(film1.getName(), "nisi eiusmod"),
                () -> assertEquals(film1.getDescription(), "adipisicing"),
                () -> assertEquals(film1.getReleaseDate(), LocalDate.of(1967, 3, 25)),
                () -> assertEquals(film1.getDuration(), Duration.ofMinutes(100))
        );
    }
}