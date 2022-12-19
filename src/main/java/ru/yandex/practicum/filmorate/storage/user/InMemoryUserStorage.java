package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

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
    public User findById(Integer id) throws EntityNotFoundException {
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException("Не найден пользователь");
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
    public User update(User user) throws EntityNotFoundException {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с логином {} обновлен", user.getLogin());
        } else {
            log.info("Такого пользователя нет: {}", user);
            throw new EntityNotFoundException("Не найден пользователь");
        }
        return user;
    }
}
