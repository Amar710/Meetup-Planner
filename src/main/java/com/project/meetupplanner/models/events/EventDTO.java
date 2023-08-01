package com.project.meetupplanner.models.events;

import java.time.LocalDateTime;

public class EventDTO {
    private Long id;
    private String text;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;
  
    // Default constructor
    public EventDTO() {
    }

    // Constructor with parameters
    public EventDTO(Long id, String text, LocalDateTime start, LocalDateTime end, String color) {
        this.id = id;
        this.text = text;
        this.start = start;
        this.end = end;
        this.color = color;
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
    
}

