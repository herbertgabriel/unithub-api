package com.unithub.dto.eventsDTOs;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;

public record CadastrarEventoDTO(
        @NotNull
        JwtAuthenticationToken authentication,
        @NotNull @NotBlank
        String title,
        @NotNull @NotBlank
        String description,
        @NotNull @Future
        LocalDateTime dateTime,
        String location,
        String category,
        String externalSubscriptionLink,
        int maxParticipants
) {
}