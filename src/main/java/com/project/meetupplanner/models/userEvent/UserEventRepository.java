package com.project.meetupplanner.models.userEvent;

import com.project.meetupplanner.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    // Find all user-events for a specific user
    List<UserEvent> findByUserUid(int uid);

    // Find all user-events for a specific event
    List<UserEvent> findByEventId(Long eventId);

       @Query("from UserEvent ue where ue.user.uid = :uid and (ue.event.start <= :end and ue.event.end >= :start)")
    List<UserEvent> findByUserUidAndEventStartAfterAndEventEndBefore(
        @Param("uid") int uid, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    
    UserEvent findByEventIdAndUser(Long eventId, User user);

    
}
