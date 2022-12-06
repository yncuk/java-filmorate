package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> giveMutualFriends(Integer userId, Integer otherUserId) throws NotFoundException {
        Set<Long> friends = userStorage.findById(userId).getFriends();
        Set<Long> friends2 = userStorage.findById(otherUserId).getFriends();
        if (friends == null || friends2 == null) {
            return new ArrayList<>();
        }
        return convertSetIdToListUsers(friends.stream().filter(friends2::contains).collect(Collectors.toSet()));
    }

    public void addFriend(Integer userId, Integer friendId) throws NotFoundException, ValidationException {
        addFriendMethod(userId, friendId);
        addFriendMethod(friendId, userId);
    }

    public void deleteFriend(Integer userId, Integer friendId) throws NotFoundException, ValidationException {
        deleteFriendMethod(userId, friendId);
        deleteFriendMethod(friendId, userId);
    }

    private List<User> convertSetIdToListUsers(Set<Long> friends) throws NotFoundException {
        List<User> userList = new ArrayList<>();
        for (long currentId : friends) {
            userList.add(userStorage.findById((int) currentId));
        }
        return userList;
    }

    private void addFriendMethod(Integer id, Integer friendId) throws NotFoundException, ValidationException {
        Set<Long> friends = userStorage.findById(friendId).getFriends();
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add((long) id);
        userStorage.update(userStorage.findById(friendId).withFriends(friends));
    }

    private void deleteFriendMethod(Integer id, Integer friendId) throws NotFoundException, ValidationException {
        Set<Long> friends = userStorage.findById(friendId).getFriends();
        if (friends == null || !friends.contains((long) id)) {
            throw new NotFoundException(String.format("Не найден %s в списке друзей %s",
                    userStorage.findById(id), userStorage.findById(friendId)));
        }
        friends.remove((long) id);
        userStorage.update(userStorage.findById(friendId).withFriends(friends));
    }
}
