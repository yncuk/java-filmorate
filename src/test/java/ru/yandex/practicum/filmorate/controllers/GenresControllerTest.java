package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.genres.GenresDbStorage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenresControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GenresController genresController;
    private final GenresDbStorage genresStorage;

    @Test
    @DisplayName("Test find all genres")
    void testFindAllGenres() {
        // when
        List<Genres> genres = (List<Genres>) genresController.findAll();
        // then
        assertAll(
                () -> assertEquals(new Genres(1, "Комедия"), genres.get(0)),
                () -> assertEquals(new Genres(2, "Драма"), genres.get(1)),
                () -> assertEquals(new Genres(3, "Мультфильм"), genres.get(2)),
                () -> assertEquals(new Genres(4, "Триллер"), genres.get(3)),
                () -> assertEquals(new Genres(5, "Документальный"), genres.get(4)),
                () -> assertEquals(new Genres(6, "Боевик"), genres.get(5))
        );

    }

    @Test
    @DisplayName("Test find by id genre")
    void testFindById() throws EntityNotFoundException {
        // when
        Genres genre = genresStorage.findById(1);
        // then
        assertEquals(new Genres(1, "Комедия"), genre);
    }

    @Test
    @DisplayName("Test find by unknown id genre")
    void testFindByUnknownId() throws Exception {
        // then
        mockMvc.perform(get("/genres/77"))
                .andExpect(status().is4xxClientError());
    }
}