package com.project.meetupplanner.models;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRespository extends JpaRepository<User, Integer> {
    List<User> findByUid(int uid);
    List<User> findByName(String name);
    List<User> findByPassword(String password);
    List<User> findByEmail(String email);
    List<User> findByNameAndPassword(String name, String password);
    List<User> findByAdmin(Boolean admin);
    User findByConfirmationCode(String confirmationCode);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.uid IN :ids")
    List<User> findAllByIdWithFriends(@Param("ids") Set<Integer> ids);
    
}


