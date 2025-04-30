package com.unithub.service;

import com.unithub.dto.request.email.EmailDTO;
import com.unithub.dto.request.login.LoginRecoverDTO;
import com.unithub.dto.request.login.LoginRequestDTO;
import com.unithub.dto.respose.login.LoginResponseDTO;
import com.unithub.exception.AuthenticationException;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
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

        return new LoginResponseDTO(jwtValue, expiresIn, scopes);
    }

    @Transactional
    public void recuperarSenha(LoginRecoverDTO dto) {
        var user = userRepository.findByEmail(dto.email());
        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid email");
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

    public User getAuthenticatedUser(JwtAuthenticationToken authentication) {
        return userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new AuthenticationException("Authenticated user not found"));
    }

}