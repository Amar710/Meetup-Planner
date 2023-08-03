package com.project.meetupplanner.models.users;


public class UserDTO {
    private String name;
    // private Set<UserEvent> userEvents;

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.name = user.getName();
        
        // this.userEvents = user.getUserEvents();
    }


    // Getters and setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

