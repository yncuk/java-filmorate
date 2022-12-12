package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

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
        film = film.withId(id);
        id++;
        films.put(film.getId(), film);
        log.info("Фильм добавлен - {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) throws NotFoundException {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен", film.getName());
        } else {
            log.info("Такого фильма нет: {}", film);
            throw new NotFoundException("Такого фильма нет");
        }
        return film;
    }
}
