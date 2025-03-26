package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private User creatorUser;

    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private boolean active;
    private int maxParticipants;

    @ElementCollection(targetClass = Categorys.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_event_categories", joinColumns = @JoinColumn(name = "event_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Set<Categorys> categorias = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @ManyToMany
    @JoinTable(
            name = "tb_event_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
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