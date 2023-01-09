package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Integer id) throws EntityNotFoundException;

    Film create(Film film) throws ValidationException;

    Film update(Film film) throws ValidationException, EntityNotFoundException;

    List<Film> giveMostPopularFilms(Integer count);
}
