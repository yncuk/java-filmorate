package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate movieBirthday = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) throws EntityNotFoundException {
        return filmStorage.findById(id);
    }

    public List<Film> giveMostPopularFilm(Integer count) {
        if (filmStorage.giveMostPopularFilms(count) == null) {
            log.info("Фильмов в списке нет, параметр = {}", filmStorage.giveMostPopularFilms(count));
            return new ArrayList<>();
        }
        log.info("Возвращается отсортированный по популярности список");
        return filmStorage.giveMostPopularFilms(count);
    }

    @SneakyThrows
    public void putLike(Integer id, Integer userId) {
        Set<Long> likedFilm = userStorage.findById(userId).getLikedFilms();
        if (likedFilm == null) {
            log.info("Пользователь {} еще не ставил лайки на фильмы", userStorage.findById(userId));
            likedFilm = new HashSet<>();
        } else if (likedFilm.contains((long) id)) {
            log.info("Пользователь {} уже поставил лайк на этот фильм - {}", userStorage.findById(userId), filmStorage.findById(id));
            return;
        }
        likedFilm.add((long) id);
        int likes = filmStorage.findById(id).getRate();
        likes++;
        filmStorage.update(filmStorage.findById(id).withRate(likes));
        userStorage.update(userStorage.findById(userId).withLikedFilms(likedFilm));
    }

    @SneakyThrows
    public void deleteLike(Integer id, Integer userId) {
        Set<Long> likedFilm = userStorage.findById(userId).getLikedFilms();
        if (likedFilm == null) {
            return;
        } else if (!likedFilm.contains((long) id)) {
            throw new EntityNotFoundException("Не найден фильм в понравившихся");
        }
        likedFilm.remove((long) id);
        int likes = filmStorage.findById(id).getRate();
        likes--;
        filmStorage.update(filmStorage.findById(id).withRate(likes));
        userStorage.update(userStorage.findById(userId).withLikedFilms(likedFilm));
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
