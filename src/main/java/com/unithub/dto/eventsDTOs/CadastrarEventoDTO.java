package com.unithub.dto;

import java.time.LocalDateTime;

public record CadastrarEventoDTO(
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