package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    @SneakyThrows
    public List<User> giveMutualFriends(Integer userId, Integer otherUserId) {
        Set<Long> friends = userStorage.findById(userId).getFriends();
        Set<Long> friends2 = userStorage.findById(otherUserId).getFriends();
        if (friends == null || friends2 == null) {
            return new ArrayList<>();
        }
        //friends.retainAll(friends2);
        //return convertSetIdToListUsers(friends);
        return convertSetIdToListUsers(friends.stream().filter(friends2::contains).collect(Collectors.toSet()));
    }

    public void addFriend(Integer userId, Integer friendId) {
        addFriendMethod(userId, friendId);
        addFriendMethod(friendId, userId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        deleteFriendMethod(userId, friendId);
        deleteFriendMethod(friendId, userId);
    }

    @SneakyThrows
    public User create(User user) {
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
        Set<Long> friends = userStorage.findById(friendId).getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add((long) id);
        userStorage.update(userStorage.findById(friendId).withFriends(friends));
    }

    @SneakyThrows
    private void deleteFriendMethod(Integer id, Integer friendId) {
        Set<Long> friends = userStorage.findById(friendId).getFriends();
        if (friends == null || !friends.contains((long) id)) {
            throw new NotFoundException(String.format("Не найден %s в списке друзей %s",
                    userStorage.findById(id), userStorage.findById(friendId)));
        }
        friends.remove((long) id);
        userStorage.update(userStorage.findById(friendId).withFriends(friends));
    }

    private void validating(User user) throws ValidationException {
        if (user.getLogin().contains(" ")) {
            //log.info("Логин: '{}' пустой или содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            //log.info("Дата рождения {} не может быть в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
