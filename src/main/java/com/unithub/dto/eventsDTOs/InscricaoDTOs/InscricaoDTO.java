package com.unithub.dto.eventsDTOs.EnrollDTOs;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public record InscricaoDTO(
        UUID eventId,
        JwtAuthenticationToken authentication
) {
}
