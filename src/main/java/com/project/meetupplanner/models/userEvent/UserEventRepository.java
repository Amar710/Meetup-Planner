package com.project.meetupplanner.models.userEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    // Find all user-events for a specific user
    List<UserEvent> findByUserUid(int uid);

    // Find all user-events for a specific event
    List<UserEvent> findByEventId(Long eventId);

    List<UserEvent> findByUserUidAndEventStartAfterAndEventEndBefore(int uid, LocalDateTime start, LocalDateTime end);

}
