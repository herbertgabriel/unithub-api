package com.unithub.dto.eventsDTOs;

import java.time.LocalDateTime;

public record AtualizarEventoDTO(String title,
                                 String description,
                                 String category,
                                 String location,
                                 LocalDateTime dateTime,
                                 String externalSubscriptionLink,
                                 Integer maxParticipants) {
}
