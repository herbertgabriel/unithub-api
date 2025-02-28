package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_events")
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID eventId;

    @CreationTimestamp
    private Instant creationTimeStamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private String category;
    private boolean active;
    private String externalSubscriptionLink;
    private int numberOfSubscribers;
    private int maxParticipants;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> enrolledUserList;

    public void addUser(User user) {
        if (!enrolledUserList.contains(user)) {
            enrolledUserList.add(user);
        }
    }
    
    public void removeUser(User user) {
        enrolledUserList.remove(user);
    }
}
