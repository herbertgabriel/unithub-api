package com.unithub.controller;

import com.unithub.dto.request.login.RedefinirSenhaDTO;
import com.unithub.dto.request.login.LoginRecoverDTO;
import com.unithub.dto.request.login.LoginRequestDTO;
import com.unithub.dto.respose.login.LoginResponseDTO;
import com.unithub.dto.request.login.CreateUserDTO;
import com.unithub.service.AuthService;
import com.unithub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDTO dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/recover-password")
    public ResponseEntity<Void> recuperarSenha(@RequestBody LoginRecoverDTO dto) {
        authService.recuperarSenha(dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Void> redefinirSenha(@RequestBody RedefinirSenhaDTO dto, JwtAuthenticationToken authentication) {
        userService.redefinirSenha(dto, authentication);
        return ResponseEntity.ok().build();
    }
}