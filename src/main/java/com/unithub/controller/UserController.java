package com.unithub.controller;

import com.unithub.dto.userDTOs.AlterarRoleDTO;
import com.unithub.dto.userDTOs.ListarUsersResponseDTO;
import com.unithub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/change-role")
    public ResponseEntity<Void> alterarRoleUsuario(@RequestBody AlterarRoleDTO dto, JwtAuthenticationToken authentication) {
        userService.alterarRoleUsuario(dto, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<ListarUsersResponseDTO>> listarUsuariosPorRole(@RequestParam long roleId, JwtAuthenticationToken authentication) {
        List<ListarUsersResponseDTO> usuarios = userService.listarUsuariosPorRole(roleId, authentication);
        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable UUID userId, JwtAuthenticationToken authentication) {
        userService.deletarUsuario(userId, authentication);
        return ResponseEntity.noContent().build();
    }

    // CRUD Gerenciamento de Representantes


}