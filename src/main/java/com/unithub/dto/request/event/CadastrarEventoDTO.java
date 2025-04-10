package com.unithub.dto.request.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public record CadastrarEventoDTO(
        @NotNull @NotBlank
        String title,
        @NotNull @NotBlank
        String description,
        @NotNull @Future
        LocalDateTime dateTime,
        String location,
        Set<Long> categoriaIds,
        int maxParticipants
) {
}