package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;


    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> giveMostPopularFilm(Integer count) {
        if (filmStorage.findAll() == null) {
            return new ArrayList<>();
        }
        Comparator<Film> comparator = (o1, o2) -> {
            if (o1.getLikes() != null && o2.getLikes() != null)
                return Integer.compare(o2.getLikes().size(), o1.getLikes().size());
            else if (o1.getLikes() != null) {
                return -1;
            } else if (o2.getLikes() != null) {
                return 1;
            } else return 0;
        };
        List<Film> filmList = new ArrayList<>(filmStorage.findAll());
        filmList.sort(comparator);
        return filmList.stream().limit(count).collect(Collectors.toList());
    }

    public void putLike(Integer id, Integer userId) throws NotFoundException, ValidationException {
        Set<Long> likes;
        userStorage.findById(userId);
        if (filmStorage.findById(id).getLikes() == null) {
            likes = new HashSet<>();
        } else {
            likes = filmStorage.findById(id).getLikes();
        }
        likes.add((long) userId);
        filmStorage.update(filmStorage.findById(id).withLikes(likes));
    }

    public void deleteLike(Integer id, Integer userId) throws NotFoundException, ValidationException {
        Set<Long> likes;
        userStorage.findById(userId);
        if (filmStorage.findById(id).getLikes() == null) {
            likes = new HashSet<>();
        } else {
            likes = filmStorage.findById(id).getLikes();
        }
        if (!likes.contains((long) userId)) {
            throw new NotFoundException(String.format("На фильме %s не найден лайк пользователя %s",
                    filmStorage.findById(id), userStorage.findById(userId)));
        }
        likes.remove((long) userId);
        filmStorage.update(filmStorage.findById(id).withLikes(likes));
    }
}
