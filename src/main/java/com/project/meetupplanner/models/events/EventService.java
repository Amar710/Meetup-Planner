package com.project.meetupplanner.models.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.meetupplanner.models.userEvent.UserEvent;
import com.project.meetupplanner.models.userEvent.UserEventRepository;
import com.project.meetupplanner.models.users.User;
import com.project.meetupplanner.models.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.project.meetupplanner.models.events.Event;
import com.project.meetupplanner.models.events.EventDTO;


@Service
@Transactional
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

    public List<Event> findUserEventsByUidAndEventStartAndEnd(int uid, LocalDateTime start, LocalDateTime end) {
        List<UserEvent> userEvents = userEventRepository.findByUserUidAndEventStartAfterAndEventEndBefore(uid, start, end);
        return userEvents.stream()
                .map(UserEvent::getEvent)
                .collect(Collectors.toList());
    }

    public EventDTO convertToEventDTO(Event event) {
        return new EventDTO(event.getId(), event.getText(), event.getStart(), event.getEnd(), event.getColor());
    }

    public List<EventDTO> getAllEvents() {
        return StreamSupport.stream(eventRepository.findAll().spliterator(), false)
                .map(this::convertToEventDTO)
                .collect(Collectors.toList());
    }
    
}

