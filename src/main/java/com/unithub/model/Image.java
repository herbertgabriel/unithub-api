package com.unithub.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "tb_images")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id")
    private UUID imageId;
    private String urlS3;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
