package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    // Не совсем понял как писать тесты к полям, которые проверяются по аннотации @Valid

    @Test
    @DisplayName("Check create film with description size more 200")
    void createFilmWithDescriptionMoreThen200() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("test test test test test test test test test test test test test test test test test " +
                "test test test test test test test test test test test test test test test test test test test " +
                "test test test test test test test test test test test test test test test test test test test " +
                "test test test test test test test test test test test test test test test test test test test " +
                "test test test test test test test test test test test test test test test test test test test ");
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    @DisplayName("Check create film with release date before movie birthday")
    void createFilmWithReleaseDateBeforeMovieBirthday() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("test");
        film.setReleaseDate(LocalDate.of(1800, 10, 12));
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Дата релиза не может быть раньше дня рождения кино"));
    }

    @Test
    @DisplayName("Check create film with negative duration")
    void createFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("test");
        film.setReleaseDate(LocalDate.of(2000, 10, 12));
        film.setDuration(Duration.ofMinutes(-100));
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertTrue(thrown.getMessage().contains("Продолжительность фильма должна быть положительной"));
    }

    @Test
    @DisplayName("Check create film without mistakes")
    void createFilmWithoutMistakes() throws ValidationException {
        Film film = new Film();
        film.setName("nisi eiusmod");
        film.setDescription("adipisicing");
        film.setReleaseDate(LocalDate.of(1967, 3, 25));
        film.setDuration(Duration.ofMinutes(100));
        Film film1 = filmController.create(film);
        assertAll(
                () -> assertEquals(film1.getId(), 1),
                () -> assertEquals(film1.getName(), "nisi eiusmod"),
                () -> assertEquals(film1.getDescription(), "adipisicing"),
                () -> assertEquals(film1.getReleaseDate(), LocalDate.of(1967, 3, 25)),
                () -> assertEquals(film1.getDuration(), Duration.ofMinutes(100))
        );
    }
}