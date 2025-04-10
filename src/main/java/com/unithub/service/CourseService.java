package com.unithub.service;

import com.unithub.model.Category;
import com.unithub.model.Course;
import com.unithub.model.Role;
import com.unithub.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final AuthService authService;

    public CourseService(CourseRepository courseRepository, AuthService authService) {
        this.courseRepository = courseRepository;
        this.authService = authService;
    }

    // Adicionar Admin
    @Transactional
    public Course criarCurso(String nome, long categoriaId, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new AccessDeniedException("You don't have permission");
        }

        Category categoria = Category.fromId(categoriaId);

        Course curso = new Course();
        curso.setNome(nome);
        curso.setCategoria(categoria);

        return courseRepository.save(curso);
    }

    @Transactional
    public void deletarCurso(Long cursoId, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new AccessDeniedException("You don't have permission");
        }

        var curso = courseRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
        courseRepository.delete(curso);
    }

    public List<Course> listarCursos() {
        return courseRepository.findAll();
    }

    public List<Category> listarCategorias() {
        return List.of(Category.values());

    }
}