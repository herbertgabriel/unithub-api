package com.unithub.dto.respose.subscribe;

import java.util.UUID;

public record InscricoesListDTO(
        UUID id,
        String name,
        String telephone,
        String email
) {
}
