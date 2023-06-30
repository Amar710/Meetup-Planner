package com.project.meetupplanner.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRespository extends JpaRepository<User,Integer> {
    List<User> findByName(String name);
    List<User> findByPassword(String password);
    List<User> findByEmail(String email);
}
