    package com.project.meetupplanner.models.events;

    import jakarta.persistence.*;

    import java.time.LocalDateTime;
    import java.util.HashSet;
    import java.util.Set;

    import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
    import com.fasterxml.jackson.databind.annotation.JsonSerialize;
    import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
    import com.project.meetupplanner.models.userEvent.UserEvent;

    import com.fasterxml.jackson.annotation.JsonIdentityInfo;
    import com.fasterxml.jackson.annotation.ObjectIdGenerators;

    @JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class, 
    property = "id")
    @Entity
    @Table(name = "event")
    public class Event {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        String text;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @Column(name = "event_start")
        LocalDateTime start;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @Column(name = "event_end")
        LocalDateTime end;

        String color;

        @Embedded
        private Location location;
    
        // Getter and setter methods for location
        public Location getLocation() {
            return location;
        }
    
        public void setLocation(Location location) {
            this.location = location;
        }

        @JsonDeserialize
        @Embeddable
        public static class Location {
            Double latitude;
            Double longitude;
            String address;

            public Double getLatitude() {
                return latitude;
            }
        
            public void setLatitude(Double latitude) {
                this.latitude = latitude;
            }
        
            public Double getLongitude() {
                return longitude;
            }
        
            public void setLongitude(Double longitude) {
                this.longitude = longitude;
            }
            public String getAddress() { // Added getter
                return address;
            }
    
            public void setAddress(String address) { // Added setter
                this.address = address;
            }
        }

        @OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        private Set<UserEvent> userEvents = new HashSet<>();
        
        


        public Long getId() {
            return id;
        }

        public void setId(  Long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public void setEnd(LocalDateTime end) {
            this.end = end;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Set<UserEvent> getUserEvents() {
            return userEvents;
        }

        public void setUserEvents(Set<UserEvent> userEvents) {
            this.userEvents = userEvents;
        }

    }
