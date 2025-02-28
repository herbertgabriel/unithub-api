package com.unithub.dto.eventsDTOs.InscricaoDTOs;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public record InscricaoDTO(
        @NotNull
        UUID eventId,
        @NotNull
        JwtAuthenticationToken authentication
) {
}
