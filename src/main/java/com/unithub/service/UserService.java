package com.unithub.service;

import com.unithub.dto.request.userManagment.AlterarRoleDTO;
import com.unithub.dto.request.login.CreateUserDTO;
import com.unithub.dto.respose.userManagment.ListarUsersResponseDTO;
import com.unithub.dto.request.login.RedefinirSenhaDTO;
import com.unithub.model.Course;
import com.unithub.model.Event;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.CourseRepository;
import com.unithub.repository.EventRepository;
import com.unithub.repository.RoleRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final CourseRepository courseRepository;
    private final ImageService imageService;
    private final EventRepository eventRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, CourseRepository courseRepository, ImageService imageService, EventRepository eventRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.courseRepository = courseRepository;
        this.imageService = imageService;
        this.eventRepository = eventRepository;
        this.authService = authService;
    }

    @Transactional
    public void createUser(CreateUserDTO dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        var basicRole = roleRepository.findByName(Role.Values.ALUNO.name());
        if (basicRole == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role 'ALUNO' not registered");
        }

        var userFromDb = userRepository.findByEmail(dto.email());
        if (userFromDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Email already registered");
        }

        var curso = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + dto.courseId()));


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
        var usuario = authService.getAuthenticatedUser(authentication);

        usuario.setPassword(bCryptPasswordEncoder.encode(dto.password()));

        userRepository.save(usuario);
    }

    // ADMIN
    @Transactional
    public void alterarRoleUsuario(AlterarRoleDTO dto, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = authService.getAuthenticatedUser(authentication);

        boolean isAdmin = usuarioAutenticado.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));
    
        boolean isOrganizador = usuarioAutenticado.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()));
    
        if (!isAdmin && (!isOrganizador || (dto.roleId() != 3L && dto.roleId() != 4L))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to perform this action");
        }
    
        var usuario = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
        if (usuario.getRoles().stream().anyMatch(role ->
                role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change admin user");
        }
    
        var novaRole = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    
        if (!isAdmin && dto.roleId() == Role.Values.ADMIN.getRoleId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot assign admin role");
        }
    
        usuario.getRoles().clear();
        usuario.getRoles().add(novaRole);
        userRepository.save(usuario);
    }

    @Transactional
    public List<ListarUsersResponseDTO> listarUsuariosPorRole(long roleId, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = authService.getAuthenticatedUser(authentication);


        boolean isAdmin = usuarioAutenticado.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));
        boolean isOrganizador = usuarioAutenticado.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()));
    
        if (!isAdmin && (!isOrganizador || (roleId != 3L && roleId != 4L))) {
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

    @Transactional
    public void deletarUsuario(UUID userId, JwtAuthenticationToken authentication) {
        var usuarioAutenticado = authService.getAuthenticatedUser(authentication);

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
    
        var eventosCriados = eventRepository.findByCreatorUser(usuario);
    
        for (Event evento : eventosCriados) {
            imageService.deleteAllEventImages(evento);
        }
    
        eventRepository.deleteAll(eventosCriados);
    
        usuario.getRoles().clear();
        userRepository.save(usuario);

        userRepository.delete(usuario);
    }

}