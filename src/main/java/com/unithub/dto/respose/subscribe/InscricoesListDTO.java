package com.unithub.dto.eventsDTOs.Inscricao;

import java.util.UUID;

public record InscricoesListDTO(
        UUID id,
        String name,
        String telephone,
        String email
) {
}
