package com.unithub.dto.userDTOs;

import jakarta.validation.constraints.*;

public record CreateUserDTO (
        @NotNull @NotBlank @Email
        String email,
        @NotNull @NotBlank @Size(min = 6, message = "A senha deve ter mínimo 6 caracteres")
        String password,
        @NotNull @NotBlank @Size(min = 6, message = "A confirmação de senha deve ter mínimo 6 caracteres")
        String confirmPassword,
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 dígitos numéricos")
        String telephone,
        @NotNull @NotBlank @Size(min = 3, message = "O nome deve ter no mínimo 3 caracteres")
        String name,
        @NotNull(message = "O ID do curso é obrigatório")
        Long courseId
){
}