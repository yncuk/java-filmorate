package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Всего пользователей {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        validating(user);
        if (user.getName() == null) {
            log.info("Меняем пустое имя {} на логин {}", user.getName(), user.getLogin());
            user = user.toBuilder().name(user.getLogin()).build();
        } else if (user.getName().isBlank()) {
            log.info("Меняем пустое имя {} на логин {}", user.getName(), user.getLogin());
            user = user.toBuilder().name(user.getLogin()).build();
        }
        user = user.toBuilder().id(id).build();
        id++;
        users.put(user.getId(), user);
        log.info("Пользователь с логином {} добавлен", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws ValidationException {
        validating(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с логином {} обновлен", user.getLogin());
        } else {
            log.info("Такого пользователя нет: {}", user);
            throw new ValidationException("Такого пользователя нет");
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
