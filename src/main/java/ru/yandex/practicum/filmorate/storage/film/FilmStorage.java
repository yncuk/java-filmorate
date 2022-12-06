package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Integer id) throws NotFoundException;

    Film create(Film film) throws ValidationException;

    Film update(Film film) throws ValidationException, NotFoundException;
}
