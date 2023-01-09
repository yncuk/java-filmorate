package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String requestAllFilms = "select * from films";
        return jdbcTemplate.query(requestAllFilms, this::makeFilm);
    }

    @Override
    public Film findById(Integer id) throws EntityNotFoundException {
        String requestFilmById = "select * from films where film_id = ?";
        if (jdbcTemplate.query(requestFilmById, this::makeId, id).isEmpty()) {
            throw new EntityNotFoundException("Не найден фильм");
        }
        return jdbcTemplate.queryForObject(requestFilmById, this::makeFilm, id);
    }

    @Override
    public Film create(Film film) throws ValidationException {
        String filmCreationRequest = "insert into films (film_name, description, release_date, duration, rate) values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                    PreparedStatement stmt = connection.prepareStatement(filmCreationRequest, new String[]{"film_id"});
                    stmt.setString(1, film.getName());
                    stmt.setString(2, film.getDescription());
                    stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
                    stmt.setInt(4, film.getDuration());
                    stmt.setInt(5, film.getRate());
                    return stmt;
                }, keyHolder);
        Integer filmId = (Integer) keyHolder.getKey();

        if (film.getMpa() != null) {
            String requestToCreateCategoryFilm = "insert into film_category (film_id, category_id) values (?, ?)";
            jdbcTemplate.update(requestToCreateCategoryFilm, filmId, film.getMpa().getId());
        }

        String requestToCreateGenresFilm = "insert into film_genre (film_id, genre_id) values (?, ?)";
        if (film.getGenres() != null) {
            for (Genres genres : film.getGenres()) {
                jdbcTemplate.update(requestToCreateGenresFilm, filmId, genres.getId());
            }
        }
        return film.withId(filmId);
    }

    @Override
    public Film update(Film film) throws ValidationException, EntityNotFoundException {
        findById(film.getId());
        String filmUpdateRequest = "update films set film_name = ?, description = ?, release_date = ?, duration = ?, rate = ? where film_id = ?";
        jdbcTemplate.update(filmUpdateRequest, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getRate(), film.getId());

        if (film.getMpa() != null) {
            String requestDeleteCategoryFilm = "delete from film_category where film_id = ?;";
            jdbcTemplate.update(requestDeleteCategoryFilm, film.getId());
            String requestUpdateCategoryFilm = "insert into film_category (film_id, category_id) values (?, ?)";
            jdbcTemplate.update(requestUpdateCategoryFilm, film.getId(), film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            String requestDeleteGenresFilm = "delete from film_genre where film_id = ?;";
            jdbcTemplate.update(requestDeleteGenresFilm, film.getId());
            String requestUpdateGenresFilm = "insert into film_genre (film_id, genre_id) values (?, ?)";
            for (Genres currentGenre : film.getGenres()) {
                jdbcTemplate.update(requestUpdateGenresFilm, film.getId(), currentGenre.getId());
            }
        }
        String updatedFilmRequest = "select * from films where film_id = ?";
        return jdbcTemplate.queryForObject(updatedFilmRequest, this::makeFilm, film.getId());
    }

    @Override
    public List<Film> giveMostPopularFilms(Integer limit) {
        String requestMostPopularFilms = "select * from films order by rate desc limit ?";
        return jdbcTemplate.query(requestMostPopularFilms, this::makeFilm, limit);
    }

    private Film makeFilm(ResultSet rs, Integer rowNum) throws SQLException {
        return new Film(rs.getInt("film_id"), rs.getString("film_name"),
                rs.getString("description"), rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"), rs.getInt("rate"),
                makeGenresForFilm(rs.getInt("film_id")), makeMpaForFilm(rs.getInt("film_id")));
    }

    private Integer makeId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("film_id");
    }

    private Integer makeCategoryId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("category_id");
    }

    private Integer makeGenreId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("genre_id");
    }

    private LinkedHashSet<Genres> makeGenresForFilm(Integer id) {
        String requestGenresId = "select genre_id from film_genre where film_id = ?";
        List<Integer> genreId = jdbcTemplate.query(requestGenresId, this::makeGenreId, id);
        String requestGenresFilm = "select * from genre where genre_id = ?";
        LinkedHashSet<Genres> genres = new LinkedHashSet<>();
        for (Integer currentGenre : genreId) {
            genres.add(new Genres(currentGenre, jdbcTemplate.queryForObject(requestGenresFilm, this::makeNameGenre, currentGenre)));
        }
        return genres;
    }

    private String makeNameGenre(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("genre_name");
    }

    private Mpa makeMpaForFilm(Integer id) {
        String requestCategoryId = "select category_id from film_category where film_id = ?";
        Integer categoryId = jdbcTemplate.queryForObject(requestCategoryId, this::makeCategoryId, id);
        String requestCategoryFilm = "select * from category where category_id = ?";
        String nameCategory = jdbcTemplate.queryForObject(requestCategoryFilm, this::makeNameCategory, categoryId);
        return new Mpa(categoryId, nameCategory);
    }

    private String makeNameCategory(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("category_name");
    }
}
