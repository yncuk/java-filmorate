package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Comparator<Film> COMPARATOR = (o1, o2) ->
        Integer.compare(o2.getLikes(), o1.getLikes());
    private final LocalDate movieBirthday = LocalDate.of(1895, 12, 28);

    public List<Film> giveMostPopularFilm(Integer count) {
        if (filmStorage.findAll() == null) {
            return new ArrayList<>();
        }
        List<Film> filmList = new ArrayList<>(filmStorage.findAll());
        filmList.sort(COMPARATOR);
        return filmList.stream().limit(count).collect(Collectors.toList());
    }

    @SneakyThrows
    public void putLike(Integer id, Integer userId) {
        Set<Long> likedFilm = userStorage.findById(userId).getLikedFilm();
        if (likedFilm == null) {
            likedFilm = new HashSet<>();
        } else if (likedFilm.contains((long) id)) {
            return;
        }
        likedFilm.add((long) id);
        int likes = filmStorage.findById(id).getLikes();
        likes++;
        filmStorage.update(filmStorage.findById(id).withLikes(likes));
        userStorage.update(userStorage.findById(userId).withLikedFilm(likedFilm));
    }

    @SneakyThrows
    public void deleteLike(Integer id, Integer userId) {
        Set<Long> likedFilm = userStorage.findById(userId).getLikedFilm();
        if (likedFilm == null) {
            return;
        } else if (!likedFilm.contains((long) id)) {
            throw new NotFoundException("Не найден фильм в понравившихся");
        }
        likedFilm.remove((long) id);
        int likes = filmStorage.findById(id).getLikes();
        likes--;
        filmStorage.update(filmStorage.findById(id).withLikes(likes));
        userStorage.update(userStorage.findById(userId).withLikedFilm(likedFilm));
    }

    public Film create(Film film) throws ValidationException {
        validating(film);
        return filmStorage.create(film);
    }

    @SneakyThrows
    public Film update(Film film) {
        validating(film);
        return filmStorage.update(film);
    }

    private void validating(Film film) throws ValidationException {
        if (film.getDescription().length() > 200) {
            //log.info("Длина описания {} > 200", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        } else if (film.getReleaseDate().isBefore(movieBirthday)) {
            //log.info("Дата релиза раньше {} и равна - {}", movieBirthday, film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше дня рождения кино");
        } else if (film.getDuration() < 0) {
            //log.info("Продолжительность фильма {} < 0", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
