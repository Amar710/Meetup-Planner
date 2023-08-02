package com.project.meetupplanner.models.users;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUid(int uid);
    Optional<User> findByName(String name);
    List<User> findByPassword(String password);
    User findByEmail(String email);
    List<User> findByNameAndPassword(String name, String password);
    List<User> findByAdmin(Boolean admin);
    User findByConfirmationCode(String confirmationCode);
    List<User> findByResetPasswordToken(String token);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.uid IN :ids")
    List<User> findAllByIdWithFriends(@Param("ids") Set<Integer> ids);
}
