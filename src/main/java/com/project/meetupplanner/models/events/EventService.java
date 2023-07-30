package com.project.meetupplanner.models.events;

// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.hibernate.Hibernate;

import com.project.meetupplanner.models.userEvent.UserEvent;
import com.project.meetupplanner.models.userEvent.UserEventRepository;
import com.project.meetupplanner.models.users.User;
import com.project.meetupplanner.models.users.UserRepository;

@Service
public class EventService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserEventRepository userEventRepository;

    public void addUserToEvent(int userId, Long eventId) {
        User user = userRepository.findByUid(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setEvent(event);

        user.getUserEvents().add(userEvent);
        event.getUserEvents().add(userEvent);

        userEventRepository.save(userEvent);
    }
}

