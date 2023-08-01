package com.project.meetupplanner.models.events;

import java.time.LocalDateTime;

import com.project.meetupplanner.models.events.Event.Location;

public class EventDTO {
    private Long id;
    private String text;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;
    private Location location; 
  
    // Default constructor
    public EventDTO() {
    }

    // Constructor with parameters
    public EventDTO(Long id, String text, LocalDateTime start, LocalDateTime end, String color, Location location) {
        this.id = id;
        this.text = text;
        this.start = start;
        this.end = end;
        this.color = color;
        this.location = location;
    }


    public long getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public String getColor() {
        return color;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
}

