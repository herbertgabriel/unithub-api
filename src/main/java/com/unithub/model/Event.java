package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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
    private User creatorUser;

    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private boolean active;
    private int maxParticipants;
    private boolean isOfficial;

    @ElementCollection(targetClass = Category.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_event_categories", joinColumns = @JoinColumn(name = "event_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Set<Category> categorias = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private Set<String> images = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "tb_event_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> enrolledUserList;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    public void addUser(User user) {
        if (!enrolledUserList.contains(user)) {
            enrolledUserList.add(user);
        }
    }

    public void removeUser(User user) {
        enrolledUserList.remove(user);
    }


}