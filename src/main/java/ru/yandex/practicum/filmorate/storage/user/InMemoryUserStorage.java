package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    @Override
    public Collection<User> findAll() {
        log.info("Всего пользователей {}", users.size());
        return users.values();
    }

    @Override
    public User findById(Integer id) throws NotFoundException {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Не найден пользователь");
        }
        return users.get(id);
    }

    @Override
    public List<User> giveFriends(Integer id) {
        List<User> friends = new ArrayList<>();
        for (long currentId : users.get(id).getFriends()) {
            friends.add(users.get((int) currentId));
        }
        return friends;
    }

    @Override
    public User create(User user) throws ValidationException {
        validating(user);
        if (user.getName() == null) {
            log.info("Меняем пустое имя {} на логин {}", user.getName(), user.getLogin());
            user = user.withName(user.getLogin());
        } else if (user.getName().isBlank()) {
            log.info("Меняем пустое имя {} на логин {}", user.getName(), user.getLogin());
            user = user.withName(user.getLogin());
        }
        user = user.withId(id);
        id++;
        users.put(user.getId(), user);
        log.info("Пользователь с логином {} добавлен", user.getLogin());
        return user;
    }

    @Override
    public User update(User user) throws ValidationException, NotFoundException {
        validating(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с логином {} обновлен", user.getLogin());
        } else {
            log.info("Такого пользователя нет: {}", user);
            throw new NotFoundException("Не найден пользователь");
        }
        return user;
    }

    private void validating(User user) throws ValidationException {
        if (user.getLogin().contains(" ")) {
            log.info("Логин: '{}' пустой или содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Дата рождения {} не может быть в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
