package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    Map<Integer, Film> films = new HashMap<>();
    private int id = 1;
    private final Comparator<Film> COMPARATOR = (o1, o2) ->
            Integer.compare(o2.getRate(), o1.getRate());

    @Override
    public Collection<Film> findAll() {
        log.info("Всего фильмов {}", films.size());
        return films.values();
    }

    @Override
    public Film findById(Integer id) throws EntityNotFoundException {
        if (!films.containsKey(id)) {
            throw new EntityNotFoundException("Не найден фильм, введите другой id");
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
    public Film update(Film film) throws EntityNotFoundException {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {} обновлен", film.getName());
        } else {
            log.info("Такого фильма нет: {}", film);
            throw new EntityNotFoundException("Такого фильма нет");
        }
        return film;
    }

    @Override
    public List<Film> giveMostPopularFilms(Integer count) {
        List<Film> filmList = new ArrayList<>(findAll());
        filmList.sort(COMPARATOR);
        log.info("Возвращается отсортированный по популярности список");
        return filmList.stream().limit(count).collect(Collectors.toList());
    }
}
