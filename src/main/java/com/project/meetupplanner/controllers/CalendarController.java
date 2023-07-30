package com.project.meetupplanner.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.project.meetupplanner.models.events.Event;
import com.project.meetupplanner.models.events.EventDTO;
import com.project.meetupplanner.models.events.EventRepository;
import com.project.meetupplanner.models.events.EventService;
import com.project.meetupplanner.models.userEvent.UserEvent;
import com.project.meetupplanner.models.userEvent.UserEventRepository;
import com.project.meetupplanner.models.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
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
                    .map(eventService::convertToEventDTO)
                    .collect(Collectors.toList());
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
        er.save(e);
    
        // Add event to user_event
        UserEvent userEvent = new UserEvent();
        userEvent.setUser(user);
        userEvent.setEvent(e);
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

    @PostMapping("/api/events/delete")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    EventDeleteResponse deleteEvent(HttpServletRequest request, @RequestBody EventDeleteParams params) {
    
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("session_user");
    
        // Delete from user_event
        UserEvent userEvent = uer.findByEventIdAndUser(params.id, user); // Assuming there's a method to fetch UserEvent by eventId and User
        if(userEvent != null) {
            uer.delete(userEvent);
        }
    
        // Delete event
        er.deleteById(params.id);
    
        return new EventDeleteResponse() {{
            message = "Deleted";
        }};
    }

    public static class EventDeleteParams {
        public Long id;
    }

    public static class EventDeleteResponse {
        public String message;
    }

    public static class EventCreateParams {
        public LocalDateTime start;
        public LocalDateTime end;
        public String text;
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