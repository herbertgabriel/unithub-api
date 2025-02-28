package com.unithub.dto.eventsDTOs;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;

public record CadastrarEventoDTO(
        JwtAuthenticationToken authentication,
        String title,
        String description,
        LocalDateTime dateTime,
        String location,
        String category,
        String externalSubscriptionLink,
        int maxParticipants
) {
}