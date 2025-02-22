package com.unithub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDetailsDTO(
    UUID eventId,
    String title,
    String description,
    LocalDateTime dateTime,
    String location,
    String category,
    boolean active,
    boolean hasCheckin,
    boolean hasCertificate,
    String externalSubscriptionLink,
    int maxParticipants
) {
}