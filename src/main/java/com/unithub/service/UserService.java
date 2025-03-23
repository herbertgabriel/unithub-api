package com.unithub.service;

import com.unithub.dto.EmailDTO;
import com.unithub.dto.userDTOs.CreateUserDTO;
import com.unithub.dto.userDTOs.RedefinirSenhaDTO;
import com.unithub.dto.userDTOs.loginDTOs.LoginResponseDTO;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;
    private final JwtEncoder jwtEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    public void createUser(CreateUserDTO dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As senhas não coincidem");
        }

        var basicRole = roleRepository.findByName(Role.Values.ALUNO.name());
        if (basicRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role ALUNO não encontrado");
        }

        var userFromDb = userRepository.findByEmail(dto.email());
        if (userFromDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Email já cadastrado");
        }

        var user = new User();
        user.setEmail(dto.email());
        user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
        user.setName(dto.name());
        user.setTelephone(dto.telephone());
        user.setRoles(Set.of(basicRole));

        userRepository.save(user);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaDTO dto, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        usuario.setPassword(bCryptPasswordEncoder.encode(dto.password()));

        userRepository.save(usuario);
    }
}