package com.project.meetupplanner.controllers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.project.meetupplanner.models.events.Event;
import com.project.meetupplanner.models.events.EventRepository;
import com.project.meetupplanner.models.userEvent.UserEvent;
import com.project.meetupplanner.models.userEvent.UserEventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
public class CalendarController {

    @Autowired
    EventRepository er;

    @Autowired
    UserEventRepository uer;

    @RequestMapping("/api")
    @ResponseBody
    String home() {
        return "Welcome!";
    }


    // @GetMapping("/api/events/")
    // @JsonSerialize(using = LocalDateTimeSerializer.class)
    // Iterable<Event> eventsSession(@PathVariable("uid") int uid, 
    //                        @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start, 
    //                        @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {
    
    //     List<UserEvent> userEvents = uer.findByUserUidAndEventStartAfterAndEventEndBefore(uid, start, end);
    //     List<Event> events = userEvents.stream().map(UserEvent::getEvent).collect(Collectors.toList());
    
    //     return events;
    // }


    @GetMapping("/api/events/{uid}")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<Event> events(@PathVariable("uid") int uid, 
                           @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start, 
                           @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {
    
        List<UserEvent> userEvents = uer.findByUserUidAndEventStartAfterAndEventEndBefore(uid, start, end);
        List<Event> events = userEvents.stream().map(UserEvent::getEvent).collect(Collectors.toList());
    
        return events;
    }
    


    @PostMapping("/api/events/create")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    Event createEvent(@RequestBody EventCreateParams params) {

        Event e = new Event();
        e.setStart(params.start);
        e.setEnd(params.end);
        e.setText(params.text);
        er.save(e);

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
    EventDeleteResponse deleteEvent(@RequestBody EventDeleteParams params) {

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