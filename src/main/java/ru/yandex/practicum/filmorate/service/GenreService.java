package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genres.GenresDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenresDbStorage genresDbStorage;

    public Collection<Mpa> findAll() {
        return genresDbStorage.findAll();
    }

    public Mpa findById(Integer id) throws EntityNotFoundException {
        return genresDbStorage.findById(id);
    }
}
