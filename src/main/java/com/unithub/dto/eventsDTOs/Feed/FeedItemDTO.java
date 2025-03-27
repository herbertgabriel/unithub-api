package com.unithub.dto.eventsDTOs.Feed;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record FeedItemDTO(
    UUID eventId,
    String title,
    String description,
    LocalDateTime dateTime,
    String location,
    Set<String> category,
    boolean active,
    Set<String> images
) {
}