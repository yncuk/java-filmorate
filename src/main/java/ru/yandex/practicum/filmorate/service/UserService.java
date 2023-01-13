package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) throws EntityNotFoundException {
        return userStorage.findById(id);
    }

    public List<User> giveFriends(Integer id) {
        return userStorage.giveFriends(id);
    }

    @SneakyThrows
    public List<User> giveMutualFriends(Integer userId, Integer otherUserId) {
        Set<Long> friends = userStorage.findById(userId).getFriends();
        Set<Long> friends2 = userStorage.findById(otherUserId).getFriends();
        if (friends == null || friends2 == null) {
            return new ArrayList<>();
        }
        return convertSetIdToListUsers(friends.stream().filter(friends2::contains).collect(Collectors.toSet()));
    }

    public void addFriend(Integer userId, Integer friendId) {
        addFriendMethod(userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        deleteFriendMethod(userId, friendId);
    }

    @SneakyThrows
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Меняем пустое имя {} на логин {}", user.getName(), user.getLogin());
            user = user.withName(user.getLogin());
        }
        validating(user);
        return userStorage.create(user);
    }

    @SneakyThrows
    public User update(User user) {
        validating(user);
        return userStorage.update(user);
    }

    @SneakyThrows
    private List<User> convertSetIdToListUsers(Set<Long> friends) {
        List<User> userList = new ArrayList<>();
        for (long currentId : friends) {
            userList.add(userStorage.findById((int) currentId));
        }
        return userList;
    }

    @SneakyThrows
    private void addFriendMethod(Integer id, Integer friendId) {
        userStorage.findById(friendId);
        Set<Long> friends = userStorage.findById(id).getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add((long) friendId);
        userStorage.update(userStorage.findById(id).withFriends(friends));
    }

    @SneakyThrows
    private void deleteFriendMethod(Integer id, Integer friendId) {
        userStorage.findById(friendId);
        Set<Long> friends = userStorage.findById(id).getFriends();
        if (friends == null || !friends.contains((long) friendId)) {
            throw new EntityNotFoundException(String.format("Не найден %s в списке друзей %s",
                    userStorage.findById(friendId), userStorage.findById(id)));
        }
        friends.remove((long) friendId);
        userStorage.update(userStorage.findById(id).withFriends(friends));
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
