package com.unithub.service;

import com.unithub.dto.EmailDTO;
import com.unithub.dto.userDTOs.CreateUserDTO;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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

//    @Transactional
//    public void recuperarSenha(String email) {
//        Optional<User> userOptional = userRepository.findByEmail(email);
//        if (userOptional.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado");
//        }
//
//        User user = userOptional.get();
//        String token = UUID.randomUUID().toString();
//        // Aqui você deve salvar o token no banco de dados associado ao usuário
//        // user.setResetPasswordToken(token);
//        // userRepository.save(user);
//
//        String mensagem = "Para redefinir sua senha, clique no link abaixo:\n" +
//                "http://dominio.com/reset-password?token=" + token;
//
//        EmailDTO emailDTO = new EmailDTO(email, "UnitHub - Recuperar Senha", mensagem);
//        emailService.sendEmail(emailDTO);
//    }
}