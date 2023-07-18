package com.project.meetupplanner.models;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getUserFriends(User user) {
        Set<Integer> friendIds = user.getFriends();
        List<User> friends = userRepo.findAllByIdWithFriends(friendIds); 

        for (User friend : friends) {
            Hibernate.initialize(friend.getFriends());
        }
        
        return friends;
    }
}
