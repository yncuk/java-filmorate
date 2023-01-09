package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
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
        String requestAllUsers = "select * from users";
        return jdbcTemplate.query(requestAllUsers, this::makeUser);
    }

    @Override
    public User findById(Integer id) throws EntityNotFoundException {
        String requestUserById = "select * from users where user_id = ?";
        if (jdbcTemplate.query(requestUserById, this::makeId, id).isEmpty()) {
            throw new EntityNotFoundException("Не найден пользователь");
        }
        return jdbcTemplate.queryForObject(requestUserById, this::makeUser, id);
    }

    @Override
    public List<User> giveFriends(Integer id) {
        String requestToReturnUserFriends = "select users.user_id, users.email, users.login, users.user_name, " +
                "users.birthday, friend.friend_id " +
                "from friend " +
                "join users on friend.friend_id = users.user_id " +
                "where friend.user_id = ?";
        return jdbcTemplate.query(requestToReturnUserFriends, this::makeUser, id);
    }

    @Override
    public User create(User user) throws ValidationException {
        String userCreationRequest = "insert into users (email, login, user_name, birthday) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(userCreationRequest, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4,  Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        Integer userId = (Integer) keyHolder.getKey();
        if (user.getFriends() != null) {
            for (long friend : user.getFriends()) {
                String userFriendRequest = "insert into friend (friend_id, status, user_id) values (?, ?, ?)";
                jdbcTemplate.update(userFriendRequest, friend, true, user.getId());
            }
        }
        if (user.getLikedFilms() != null) {
            for (long likedFilm : user.getLikedFilms()) {
                String requestCreateUserLikes = "insert into liked_film (user_id, film_id) values (?, ?)";
                jdbcTemplate.update(requestCreateUserLikes, user.getId(), likedFilm);
            }
        }
        return user.withId(userId);
    }

    @Override
    public User update(User user) throws ValidationException, EntityNotFoundException {
        findById(user.getId());
        String userUpdateRequest = "update users set email = ?, login = ?, user_name = ?, birthday = ? where user_id = ?";
        jdbcTemplate.update(userUpdateRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (user.getFriends() != null) {
            String requestDeleteUserFriends = "delete from FRIEND where user_id = ?;";
            jdbcTemplate.update(requestDeleteUserFriends, user.getId());
            for (long friend : user.getFriends()) {
                String requestUpdateUserFriends = "insert into friend (friend_id, status, user_id) values (?, ?, ?)";
                jdbcTemplate.update(requestUpdateUserFriends, friend, true, user.getId());
            }
        }
        if (user.getLikedFilms() != null) {
            String requestDeleteUserLikes = "delete from liked_film where user_id = ?;";
            jdbcTemplate.update(requestDeleteUserLikes, user.getId());
            for (long likedFilm : user.getLikedFilms()) {
                String requestUpdateUserLikes = "insert into liked_film (user_id, film_id) values (?, ?)";
                jdbcTemplate.update(requestUpdateUserLikes, user.getId(), likedFilm);
            }
        }
        return user;
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
        String requestUserLikes = "select liked_film.film_id " +
                "from liked_film " +
                "join users on liked_film.user_id = users.user_id " +
                "where liked_film.user_id = ?";
        return jdbcTemplate.query(requestUserLikes, this::makeFilmId, id);
    }
}
