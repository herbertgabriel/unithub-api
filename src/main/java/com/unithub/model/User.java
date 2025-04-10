package com.unithub.model;

import com.unithub.dto.request.login.LoginRequestDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    private String email;
    private String password;
    private String telephone;
    private String name;

    @ManyToMany(mappedBy = "enrolledUserList")
    private Set<Event> events;

    @ManyToMany
    @JoinTable(
            name = "tb_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Course course;

    public boolean isLoginCorrect(LoginRequestDTO loginRequest, PasswordEncoder passwordEncoder) {
        if (loginRequest.password() == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }
}