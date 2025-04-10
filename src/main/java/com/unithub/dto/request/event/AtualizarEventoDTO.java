package com.unithub.dto.request.event;

import java.time.LocalDateTime;
import java.util.Set;

public record AtualizarEventoDTO(String title,
                                 String description,
                                 String category,
                                 String location,
                                 LocalDateTime dateTime,
                                 Integer maxParticipants,
                                 Set<Long> categoriaIds) {
}
