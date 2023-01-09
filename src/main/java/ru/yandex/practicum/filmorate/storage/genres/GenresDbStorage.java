package ru.yandex.practicum.filmorate.storage.genres;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Genres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class GenresDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Collection<Genres> findAll() {
        String sql = "select * from genre";
        return jdbcTemplate.query(sql, this::makeGenre);
    }

    public Genres findById(Integer id) throws EntityNotFoundException {
        String sql = "select * from genre where genre_id = ?";
        if (jdbcTemplate.query(sql, this::makeGenre, id).isEmpty()) {
            throw new EntityNotFoundException("Не найден жанр фильма");
        }
        return jdbcTemplate.queryForObject(sql, this::makeGenre, id);
    }

    private Genres makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genres(rs.getInt("genre_id"), rs.getString("genre_name"));
    }
}
