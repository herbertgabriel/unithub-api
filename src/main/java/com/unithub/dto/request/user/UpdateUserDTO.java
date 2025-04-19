package com.unithub.dto.request.user;

import jakarta.validation.constraints.*;

public record UpdateUserDTO(
        @Email(message = "O email deve ser válido")
        String email,
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,
        @Size(min = 6, message = "A confirmação de senha deve ter no mínimo 6 caracteres")
        String confirmPassword,
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 dígitos numéricos")
        String telephone,
        @Size(min = 3, message = "O nome deve ter no mínimo 3 caracteres")
        String name
) {
}