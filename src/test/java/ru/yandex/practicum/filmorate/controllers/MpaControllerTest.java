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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MpaController mpaController;
    private final MpaDbStorage mpaStorage;

    @Test
    @DisplayName("Test find all mpa")
    void testFindAll() {
        // when
        List<Mpa> mpa = (List<Mpa>) mpaController.findAll();
        // then
        assertAll(
                () -> assertEquals(new Mpa(1, "G"), mpa.get(0)),
                () -> assertEquals(new Mpa(2, "PG"), mpa.get(1)),
                () -> assertEquals(new Mpa(3, "PG-13"), mpa.get(2)),
                () -> assertEquals(new Mpa(4, "R"), mpa.get(3)),
                () -> assertEquals(new Mpa(5, "NC-17"), mpa.get(4))
        );
    }

    @Test
    @DisplayName("Test find by id mpa")
    void testFindById() throws EntityNotFoundException {
        // when
        Mpa mpa = mpaStorage.findById(3);
        // then
        assertEquals(new Mpa(3, "PG-13"), mpa);
    }

    @Test
    @DisplayName("Test find by unknown id mpa")
    void testFindByUnknownId() throws Exception {
        // then
        mockMvc.perform(get("/mpa/77"))
                .andExpect(status().is4xxClientError());
    }
}