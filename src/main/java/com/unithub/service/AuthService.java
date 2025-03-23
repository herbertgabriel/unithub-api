package com.unithub.service;

import com.unithub.dto.EmailDTO;
import com.unithub.dto.userDTOs.loginDTOs.LoginRecoverDTO;
import com.unithub.dto.userDTOs.loginDTOs.LoginRequestDTO;
import com.unithub.dto.userDTOs.loginDTOs.LoginResponseDTO;
import com.unithub.model.Role;
import com.unithub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    public AuthService(JwtEncoder jwtEncoder, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        var user = userRepository.findByEmail(loginRequest.email());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("Invalid email or password");
        }

        var now = Instant.now();
        var expiresIn = 300L;

        var roles = user.get().getRoles();
        var scopes = roles.stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponseDTO(jwtValue, expiresIn);
    }

    @Transactional
    public void recuperarSenha(LoginRecoverDTO dto) {
        var user = userRepository.findByEmail(dto.email());
        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        var now = Instant.now();
        var expiresIn = 100L; // 1 hora

        var roles = user.get().getRoles();
        var scopes = roles.stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        String mensagem = "Para redefinir sua senha, clique no link abaixo:\n" +
                "unithub.vercel.app/recover/" + jwtValue;

        EmailDTO emailDTO = new EmailDTO(dto.email(), "UnitHub - Recuperar Senha", mensagem);
        emailService.sendEmail(emailDTO);
    }
}