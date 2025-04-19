package com.unithub.dto.respose.user;

import java.util.UUID;

public record UserDetailsDTO(
        UUID userId,
        String name,
        String telephone,
        String email
) {
}