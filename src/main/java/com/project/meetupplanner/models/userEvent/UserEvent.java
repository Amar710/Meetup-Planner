package com.project.meetupplanner.models.userEvent;

import com.project.meetupplanner.models.events.Event;
import com.project.meetupplanner.models.users.User;

import jakarta.persistence.*;

@Entity
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private int uid;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;



    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
}
