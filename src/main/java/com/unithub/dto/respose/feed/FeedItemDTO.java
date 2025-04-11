package com.unithub.dto.respose.feed;

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
    boolean isOfficial,
    Set<String> images
) {
}