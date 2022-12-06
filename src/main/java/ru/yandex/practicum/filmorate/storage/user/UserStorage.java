package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> findAll();

    User findById(Integer id) throws NotFoundException;

    List<User> giveFriends(Integer id);

    User create(User user) throws ValidationException;

    User update(User user) throws ValidationException, NotFoundException;
}
