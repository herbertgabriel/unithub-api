package com.unithub.dto.eventsDTOs;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record EventDetailsDTO(
    UUID eventId,
    String title,
    String description,
    LocalDateTime dateTime,
    String location,
    Set<String> categories,
    boolean active,
    int maxParticipants
) {
}