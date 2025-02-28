package com.unithub.dto.eventsDTOs;

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
    String externalSubscriptionLink,
    int maxParticipants
) {
}