package com.unithub.dto.eventsDTOs;

import jakarta.validation.constraints.NotBlank;

public record RecusarEventoDTO(@NotBlank String motivo) {
}
