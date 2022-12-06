package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final LocalDate movieBirthday = LocalDate.of(1895, 12, 28);
    Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    @Override
    public Collection<Film> findAll() {
        log.info("Всего фильмов {}", films.size());
        return films.values();
    }

    @Override
    public Film findById(Integer id) throws NotFoundException {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Не найден фильм, введите другой id");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) throws ValidationException {
        validating(film);
        film = film.withId(id);
        id++;
        films.put(film.getId(), film);
        log.info("Фильм добавлен - {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) throws ValidationException, NotFoundException {
        validating(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен", film.getName());
        } else {
            log.info("Такого фильма нет: {}", film);
            throw new NotFoundException("Такого фильма нет");
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
