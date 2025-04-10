package com.unithub.dto.request.event;

import jakarta.validation.constraints.NotBlank;

public record RecusarEventoDTO(@NotBlank String motivo) {
}
