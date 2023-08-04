package com.project.meetupplanner.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Optional;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.project.meetupplanner.models.events.Event;
import com.project.meetupplanner.models.events.EventDTO;
import com.project.meetupplanner.models.events.EventRepository;
import com.project.meetupplanner.models.events.EventService;
import com.project.meetupplanner.models.userEvent.UserEvent;
import com.project.meetupplanner.models.userEvent.UserEventRepository;
import com.project.meetupplanner.models.users.UserService;
import com.project.meetupplanner.models.users.User;
import com.project.meetupplanner.models.users.UserDTO;
import com.project.meetupplanner.models.users.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class CalendarController {

    @Autowired
    EventRepository er;

    @Autowired
    private EventService eventService;

    @Autowired
    UserEventRepository uer;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService; 

    @RequestMapping("/api")
    @ResponseBody
    String home() {
        return "Welcome!";
    }

    @GetMapping("/events")
    public List<EventDTO> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/api/events")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public Iterable<EventDTO> eventsSession(HttpServletRequest request,
                                @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start,
                                @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {
        HttpSession session = request.getSession();
        User profile = (User) session.getAttribute("session_user");
        int uid = profile.getUid();
    
        List<Event> events = eventService.findUserEventsByUidAndEventStartAndEnd(uid, start, end);
        return events.stream()
                    .filter(event -> uer.findByEventAndUserUid(event, profile.getUid()).getAccepted())
                    .map(eventService::convertToEventDTO)
                    .collect(Collectors.toList());
    }
    
    @GetMapping("/api/events/{uid}")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public Iterable<EventDTO> events(@PathVariable("uid") int uid, 
                                @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start, 
                                @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {
    
        List<Event> events = eventService.findUserEventsByUidAndEventStartAndEnd(uid, start, end);
        return events.stream()
                    .filter(event -> uer.findByEventAndUserUid(event, uid).getAccepted())
                    .map(eventService::convertToEventDTO)
                    .collect(Collectors.toList());
    }
    
    
    @GetMapping("/api/events/invitations")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public Iterable<EventDTO> eventsRequestSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User profile = (User) session.getAttribute("session_user");
        int uid = profile.getUid();
    
        LocalDateTime start = LocalDateTime.now().plusYears(-1); // start is current local time
        LocalDateTime end = start.plusYears(1); // end is one year from now
    
        List<Event> events = eventService.findUserEventsByUidAndEventStartAndEnd(uid, start, end);
        return events.stream()
                    .filter(event ->  !uer.findByEventAndUserUid(event, uid).getAccepted())
                    .map(eventService::convertToEventDTO)
                    .collect(Collectors.toList());
    }
    
    @PutMapping("/api/userevent/accept/{eventId}")
    public UserEvent acceptInvitation(HttpServletRequest request, @PathVariable("eventId") Long eventId) {
        HttpSession session = request.getSession();
        User profile = (User) session.getAttribute("session_user");
        int uid = profile.getUid();

        // You might want to handle the case when the UserEvent doesn't exist
        UserEvent userEvent = uer.findByEventIdAndUserUid(eventId, uid);
        userEvent.setAccepted(true);
        return uer.save(userEvent);
    }

    
    
    @PostMapping("/api/events/create")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    Event createEvent(HttpServletRequest request, @RequestBody EventCreateParams params) {
    
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user");
    
        Event e = new Event();
        e.setStart(params.start);
        e.setEnd(params.end);
        e.setText(params.text);
    
        // Create a new Location object from the location params
        if(params.location != null){
            Event.Location location = new Event.Location();
            location.setLatitude(params.location.latitude);
            location.setLongitude(params.location.longitude);
            location.setAddress(params.location.address);
        
            // Set the location on the Event
            e.setLocation(location);
    }
        er.save(e);
    
        // Add event to user_event
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setEvent(e);
        userEvent.setAccepted(true);
        uer.save(userEvent);
    
        return e;
    }
    
    
    
    @PostMapping("/api/events/move")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    Event moveEvent(@RequestBody EventMoveParams params) {

        Event e = er.findById(params.id).get();
        e.setStart(params.start);
        e.setEnd(params.end);
        er.save(e);

        return e;
    }

    @PostMapping("/api/events/setColor")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    Event setColor(@RequestBody SetColorParams params) {

        Event e = er.findById(params.id).get();
        e.setColor(params.color);
        er.save(e);

        return e;
    }

    @PostMapping("/api/events/Remove")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional  
    EventDeleteResponse removeEvent(HttpServletRequest request, @RequestBody EventDeleteParams params) {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user");

        // Delete from user_event
        UserEvent userEvent = uer.findByEventIdAndUser(params.id, user);
        if(userEvent != null) {
            // Disassociate the user with the event
            userEvent.getUser().getUserEvents().remove(userEvent);
            userEvent.getEvent().getUserEvents().remove(userEvent);
            
            // Delete the userEvent
            uer.delete(userEvent);
        }

        // Check if there are any other UserEvent records associated with the event
        List<UserEvent> userEventsForEvent = uer.findByEventId(params.id);
        
        // If there are no other UserEvent records, delete the event
        if (userEventsForEvent == null || userEventsForEvent.isEmpty()) {
            er.deleteById(params.id);
        }

        return new EventDeleteResponse() {{
            message = "Deleted";
        }};
    }


    @PostMapping("/api/events/invite")
    @Transactional
    public ResponseEntity<Map<String, String>> inviteUser(@RequestBody EventInviteParams params) {
        // Fetch the user by username
        User user = userRepository.findByName(params.name).orElse(null);
    
        if (user == null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Invalid username."));
        }
    
        // Fetch the event by id
        Event event = er.findById(params.eventId).orElse(null);
        
        if (event == null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Invalid event id."));
        }
    
        // Check if the UserEvent already exists
        UserEvent userEvent = uer.findByEventAndUser(event, user);
        
        if (userEvent != null) {
            return ResponseEntity.ok(Collections.singletonMap("message", "User has already been invited."));
        }
    
        userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setEvent(event);
        userEvent.setAccepted(false);
        uer.save(userEvent);
    
        return ResponseEntity.ok(Collections.singletonMap("message", "User has been invited successfully."));
    }
    

    @GetMapping("/api/user/friends")
    public ResponseEntity<List<String>> getUserFriends(HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        
        if (user == null) {
            // Handle the case where the user is not logged in
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<User> friends = userService.getUserFriends(user);
        List<String> friendNames = friends.stream().map(User::getName).collect(Collectors.toList());
    
        return ResponseEntity.ok(friendNames);
    }
    


    @PostMapping("/api/events/{id}/location")
    public ResponseEntity<Void> updateEventLocation(@PathVariable Long id, @RequestBody Event.Location location) {
        System.out.println("location: " + location);  // log the location value
    
        Optional<Event> optionalEvent = er.findById(id);
    
        if (optionalEvent.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    
        Event event = optionalEvent.get();
        event.setLocation(location);
        er.save(event);
    
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/api/events/{id}/update", consumes = "application/json")
    public ResponseEntity<String> updateEventDescription(@PathVariable Long id, @RequestBody EventUpdateParams params) {
        Optional<Event> optionalEvent = er.findById(id);

        if (optionalEvent.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Event event = optionalEvent.get();
        event.setText(params.text);
        er.save(event);

        return new ResponseEntity<>("{\"message\": \"Event updated successfully\"}", HttpStatus.OK);
    }

    @GetMapping("/api/events/next/{id}")
    public ResponseEntity<EventDTO> getNextEventById(@PathVariable Long id) {
        Optional<Event> optionalEvent = er.findById(id);

        if (optionalEvent.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Event event = optionalEvent.get();
        EventDTO eventDTO = eventService.convertToEventDTO(event);

        return new ResponseEntity<>(eventDTO, HttpStatus.OK);
    }


    @GetMapping("/api/event/{id}/users")
    public List<UserDTO> getUsersRelatedToEvent(@PathVariable("id") Long eventId) {
        Optional<Event> optionalEvent = er.findById(eventId);

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            Set<UserEvent> userEvents = event.getUserEvents();

            List<UserDTO> users = userEvents.stream()
                .map(userEvent -> new UserDTO(userEvent.getUser()))
                .collect(Collectors.toList());

            // If you want to print users to the console
            users.forEach(user -> System.out.println("Name: " + user.getName()));

            return users;
        } else {
            throw new RuntimeException("Event with id " + eventId + " not found.");
        }
    }

    
    



    
    public static class EventUpdateParams {
        public String text;
    }

    public static class EventInviteParams {
        public Long eventId;
        public String name;
    }

    public static class EventDeleteParams {
        public Long id;
    }

    public static class EventDeleteResponse {
        public String message;
    }

    public static class LocationParams {
        public Double latitude;
        public Double longitude;
        public String address;
    }
    
    public static class EventCreateParams {
        public LocalDateTime start;
        public LocalDateTime end;
        public String text;
        public LocationParams location; // New field
    }
    
    

    public static class EventMoveParams {
        public Long id;
        public LocalDateTime start;
        public LocalDateTime end;
    }

    public static class SetColorParams {
        public Long id;
        public String color;
    }


}