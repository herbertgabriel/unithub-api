package com.unithub.dto.userDTOs;

import java.util.UUID;

public record ListarUsersResponseDTO(
        UUID UserId,
        String name,
        String email,
        String telephone,
        String course,
        String role
) {
}
