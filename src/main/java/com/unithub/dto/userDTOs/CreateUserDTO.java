package com.unithub.dto.userDTOs;

public record CreateUserDTO (
        String email,
        String password,
        String confirmPassword,
        String telephone,
        String name
){
}