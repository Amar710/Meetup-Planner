package com.project.meetupplanner.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRespository extends JpaRepository<User,Integer> {
    List<User> findByName(String name);
    List<User> findByPassword(String password);
    List<User> findByEmail(String email);
    List<User> findByNameAndPassword(String name, String password);
    List<User> findByAdmin(Boolean admin);
}
