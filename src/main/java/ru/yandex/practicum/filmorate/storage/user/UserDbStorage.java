package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, this::makeUser);
    }

    @Override
    public User findById(Integer id) throws EntityNotFoundException {
        String sql = "select * from users where user_id = ?";
        if (jdbcTemplate.query(sql, this::makeId, id).isEmpty()) {
            throw new EntityNotFoundException("Не найден пользователь");
        }
        return jdbcTemplate.queryForObject(sql, this::makeUser, id);
    }

    @Override
    public List<User> giveFriends(Integer id) {
        String sql = "select users.user_id, users.email, users.login, users.user_name, users.birthday, friend.friend_id from friend join users on friend.friend_id = users.user_id where friend.user_id = ? group by users.user_id";
        return jdbcTemplate.query(sql, this::makeUser, id);
    }

    @Override
    public User create(User user) throws ValidationException {
        String sql = "insert into users (email, login, user_name, birthday) values (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        if (user.getFriends() != null) {
            for (long friend : user.getFriends()) {
                String sql3 = "insert into friend (friend_id, status, user_id) values (?, ?, ?)";
                jdbcTemplate.update(sql3, friend, true, user.getId());
            }
        }
        if (user.getLikedFilms() != null) {
            for (long likedFilm : user.getLikedFilms()) {
                String sql5 = "insert into liked_film (user_id, film_id) values (?, ?)";
                jdbcTemplate.update(sql5, user.getId(), likedFilm);
            }
        }

        String sql2 = "select * from users where email = ? and login = ? and user_name = ? and birthday = ?";
        return jdbcTemplate.queryForObject(sql2, this::makeUser, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday());
    }

    @Override
    public User update(User user) throws ValidationException, EntityNotFoundException {
        findById(user.getId());
        String sql = "update users set email = ?, login = ?, user_name = ?, birthday = ? where user_id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (user.getFriends() != null) {
            String sql2 = "delete from FRIEND where user_id = ?;";
            jdbcTemplate.update(sql2, user.getId());
            for (long friend : user.getFriends()) {
                String sql3 = "insert into friend (friend_id, status, user_id) values (?, ?, ?)";
                jdbcTemplate.update(sql3, friend, true, user.getId());
            }
        }
        if (user.getLikedFilms() != null) {
            String sql4 = "delete from liked_film where user_id = ?;";
            jdbcTemplate.update(sql4, user.getId());
            for (long likedFilm : user.getLikedFilms()) {
                String sql5 = "insert into liked_film (user_id, film_id) values (?, ?)";
                jdbcTemplate.update(sql5, user.getId(), likedFilm);
            }
        }
        String sql6 = "select * from users where email = ? and login = ? and user_name = ? and birthday = ?";
        return jdbcTemplate.queryForObject(sql6, this::makeUser, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday());
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("email"),
                rs.getString("login"), rs.getString("user_name"),
                rs.getDate("birthday").toLocalDate(),
                makeFriendForUser(rs.getInt("user_id")),
                makeLikedFilmsForUser(rs.getInt("user_id")));
    }

    private Integer makeId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("user_id");
    }

    private Integer makeFilmId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("film_id");
    }

    private Set<Long> makeFriendForUser(Integer id) {
        Set<Long> setFriend = new HashSet<>();
        for (User user : giveFriends(id)) {
            setFriend.add((long) user.getId());
        }
        return setFriend;
    }

    private Set<Long> makeLikedFilmsForUser(Integer id) {
        Set<Long> setLikedFilms = new HashSet<>();
        for (Integer currentLikedFilm : giveLikedFilms(id)) {
            setLikedFilms.add((long) currentLikedFilm);
        }
        return setLikedFilms;
    }

    public List<Integer> giveLikedFilms(Integer id) {
        String sql = "select liked_film.film_id from liked_film join users on liked_film.user_id = users.user_id where liked_film.user_id = ? group by users.user_id";
        return jdbcTemplate.query(sql, this::makeFilmId, id);
    }
}
