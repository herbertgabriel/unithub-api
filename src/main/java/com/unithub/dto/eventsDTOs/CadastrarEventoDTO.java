package com.unithub.dto.eventsDTOs;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CadastrarEventoDTO(
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