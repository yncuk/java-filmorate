package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> findAll() {
        String sql = "select * from category";
        return jdbcTemplate.query(sql, this::makeMpa);
    }

    public Mpa findById(Integer id) throws EntityNotFoundException {
        String sql = "select * from category where category_id = ?";
        if (jdbcTemplate.query(sql, this::makeMpa, id).isEmpty()) {
            throw new EntityNotFoundException("Не найдена категория фильма");
        }
        return jdbcTemplate.queryForObject(sql, this::makeMpa, id);
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("category_id"), rs.getString("category_name"));
    }
}
