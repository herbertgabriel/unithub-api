package com.unithub.dto.respose.event;

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
    int maxParticipants,
    Set<String> images
) {
}