package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/*public class FilmMapper implements RowMapper<List<Long>> {

    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Film(rs.getInt("film_id"), rs.getString("film_name"),
                rs.getString("description"), rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"), rs.getInt("rate"),
                makeGenresForFilm(rs.getInt("film_id")), makeMpaForFilm(rs.getInt("film_id")));

    }
}*/
