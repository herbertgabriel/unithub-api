package com.unithub.dto.eventsDTOs.FeedDTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedItemDTO(
    UUID eventId,
    String title,
    String description,
    LocalDateTime dateTime,
    String location,
    String category,
    boolean active
) {
}