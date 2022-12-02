package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final LocalDate movieBirthday = LocalDate.of(1895, 12, 28);
    Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Всего фильмов {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        validating(film);
        film = film.withId(id);
        id++;
        films.put(film.getId(), film);
        log.info("Фильм добавлен - {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws ValidationException {
        validating(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен", film.getName());
        } else {
            log.info("Такого фильма нет: {}", film);
            throw new ValidationException("Такого фильма нет");
        }
        return film;
    }

    private void validating(Film film) throws ValidationException {
        if (film.getDescription().length() > 200) {
            log.info("Длина описания {} > 200", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        } else if (film.getReleaseDate().isBefore(movieBirthday)) {
            log.info("Дата релиза раньше {} и равна - {}", movieBirthday, film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше дня рождения кино");
        } else if (film.getDuration() < 0) {
            log.info("Продолжительность фильма {} < 0", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
