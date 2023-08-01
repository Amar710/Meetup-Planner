package com.project.meetupplanner.models.users;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    private final UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getUserFriends(User user) {
        Set<User> friendUsers = user.getFriends();

        // Extracting the user ids from the friendUsers set
        Set<Integer> friendIds = friendUsers.stream().map(User::getUid).collect(Collectors.toSet());

        List<User> friends = userRepo.findAllByIdWithFriends(friendIds); 

        for (User friend : friends) {
            Hibernate.initialize(friend.getFriends());
        }
        
        return friends;
    }

    @Transactional
    public void addFriend(Integer userId, Integer friendId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepo.findById(friendId).orElseThrow(() -> new RuntimeException("Friend not found"));
        user.addFriend(friend);
        userRepo.save(user);
    }

    @Transactional
    public void removeFriend(Integer userId, Integer friendId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepo.findById(friendId).orElseThrow(() -> new RuntimeException("Friend not found"));
        user.removeFriend(friend);
        userRepo.save(user);
    }

}



