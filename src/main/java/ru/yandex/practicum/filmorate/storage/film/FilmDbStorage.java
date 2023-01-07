package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

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
        String sql = "select * from films";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    @Override
    public Film findById(Integer id) throws EntityNotFoundException {
        String sql = "select * from films where film_id = ?";
        if (jdbcTemplate.query(sql, this::makeId, id).isEmpty()) {
            throw new EntityNotFoundException("Не найден фильм");
        }
        return jdbcTemplate.queryForObject(sql, this::makeFilm, id);
    }

    @Override
    public Film create(Film film) throws ValidationException {
        String sql = "insert into films (film_name, description, release_date, duration, rate) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getRate());

        String sql3 = "select * from films where film_name = ? and description = ? and release_date = ? and duration = ?";
        Integer filmId = jdbcTemplate.queryForObject(sql3, this::makeId, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration());

        if (film.getMpa() != null) {
            String sql2 = "insert into film_category (film_id, category_id) values (?, ?)";
            jdbcTemplate.update(sql2, filmId, film.getMpa().getId());
        }

        String sql4 = "insert into film_genre (film_id, genre_id) values (?, ?)";
        if (film.getGenres() != null) {
            for (Mpa genres : film.getGenres()) {
                jdbcTemplate.update(sql4, filmId, genres.getId());
            }
        }
        return jdbcTemplate.queryForObject(sql3, this::makeFilm, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration());
    }

    @Override
    public Film update(Film film) throws ValidationException, EntityNotFoundException {
        findById(film.getId());
        String sql = "update films set film_name = ?, description = ?, release_date = ?, duration = ?, rate = ? where film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getRate(), film.getId());

        if (film.getMpa() != null) {
            String sql2 = "delete from film_category where film_id = ?;";
            jdbcTemplate.update(sql2, film.getId());
            String sql3 = "insert into film_category (film_id, category_id) values (?, ?)";
            jdbcTemplate.update(sql3, film.getId(), film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            String sql4 = "delete from film_genre where film_id = ?;";
            jdbcTemplate.update(sql4, film.getId());
            String sql5 = "insert into film_genre (film_id, genre_id) values (?, ?)";
            for (Mpa currentMpa : film.getGenres()) {
                jdbcTemplate.update(sql5, film.getId(), currentMpa.getId());
            }
        }
        String sql6 = "select * from films where film_name = ? and description = ? and release_date = ? and duration = ?";
        return jdbcTemplate.queryForObject(sql6, this::makeFilm, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration());
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

    private LinkedHashSet<Mpa> makeGenresForFilm(Integer id) {
        String sql = "select genre_id from film_genre where film_id = ?";
        List<Integer> genreId = jdbcTemplate.query(sql, this::makeGenreId, id);
        String sql2 = "select * from genre where genre_id = ?";
        LinkedHashSet<Mpa> genres = new LinkedHashSet<>();
        for (Integer currentGenre : genreId) {
            genres.add(new Mpa(currentGenre, jdbcTemplate.queryForObject(sql2, this::makeNameGenre, currentGenre)));
        }
        return genres;
    }

    private String makeNameGenre(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("genre_name");
    }

    private Mpa makeMpaForFilm(Integer id) {
        String sql = "select category_id from film_category where film_id = ?";
        Integer categoryId = jdbcTemplate.queryForObject(sql, this::makeCategoryId, id);
        String sql2 = "select * from category where category_id = ?";
        String nameCategory = jdbcTemplate.queryForObject(sql2, this::makeNameCategory, categoryId);
        return new Mpa(categoryId, nameCategory);
    }

    private String makeNameCategory(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("category_name");
    }
}
