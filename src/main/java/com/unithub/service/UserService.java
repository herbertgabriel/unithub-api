package com.unithub.service;

import com.unithub.dto.userDTOs.AlterarRoleDTO;
import com.unithub.dto.userDTOs.CreateUserDTO;
import com.unithub.dto.userDTOs.ListarUsersResponseDTO;
import com.unithub.dto.userDTOs.RedefinirSenhaDTO;
import com.unithub.model.Course;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.CourseRepository;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;
    private final JwtEncoder jwtEncoder;
    private final CourseRepository courseRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService, JwtEncoder jwtEncoder, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
        this.jwtEncoder = jwtEncoder;
        this.courseRepository = courseRepository;
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

        var curso = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado com o ID: " + dto.courseId()));


        var user = new User();
        user.setEmail(dto.email());
        user.setPassword(bCryptPasswordEncoder.encode(dto.password()));
        user.setName(dto.name());
        user.setTelephone(dto.telephone());
        user.setRoles(Set.of(basicRole));
        user.setCourse(curso);

        userRepository.save(user);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaDTO dto, JwtAuthenticationToken authentication) {
        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        usuario.setPassword(bCryptPasswordEncoder.encode(dto.password()));

        userRepository.save(usuario);
    }

    // ADMIN

    @Transactional
    public void alterarRoleUsuario(AlterarRoleDTO dto, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (usuarioAutenticado.getRoles().stream().noneMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to perform this action");
        }

        var usuario = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (usuario.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change admin user");
        }

        var novaRole = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if(dto.roleId() == 1){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add a admin user");
        }

        usuario.getRoles().clear();

        usuario.getRoles().add(novaRole);

        userRepository.save(usuario);
    }

    @Transactional
    public List<ListarUsersResponseDTO> listarUsuariosPorRole(long roleId, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (usuarioAutenticado.getRoles().stream().noneMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to perform this action");
        }

        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        var usuarios = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(role))
                .toList();

        return usuarios.stream()
                .map(user -> new ListarUsersResponseDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getTelephone(),
                        Optional.ofNullable(user.getCourse()).map(Course::getNome).orElse(null), // Usa Optional para lidar com null
                        role.getName()
                ))
                .toList();
    }

    public void deletarUsuario(UUID userId, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (usuarioAutenticado.getRoles().stream().noneMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to perform this action");
        }

        var usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (usuario.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin user");
        }

        usuario.getRoles().clear();
        userRepository.save(usuario);

        userRepository.delete(usuario);
    }

}