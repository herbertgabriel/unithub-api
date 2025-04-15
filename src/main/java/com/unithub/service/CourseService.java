package com.unithub.service;

import com.unithub.dto.request.course.EditarCursoDTO;
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

    @Transactional
    public Course criarCurso(String nome, long categoriaId, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new AccessDeniedException("You don't have permission");
        }

        validarCategoriaId(categoriaId);

        Category categoria = Category.fromId(categoriaId);

        Course curso = new Course();
        curso.setNome(nome);
        curso.setCategoria(categoria);

        return courseRepository.save(curso);
    }

    @Transactional
    public Course editarCurso(EditarCursoDTO dto, long cursoId, JwtAuthenticationToken authentication) {

        var usuario = authService.getAuthenticatedUser(authentication);

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (!isOrganizadorOrAdmin) {
            throw new AccessDeniedException("You don't have permission");
        }

        var curso = courseRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (dto.nome() != null) {
            curso.setNome(dto.nome());
        }

        if (dto.categoriaId() != 0) {
            validarCategoriaId(dto.categoriaId());
            Category categoria = Category.fromId(dto.categoriaId());
            curso.setCategoria(categoria);
        }

        return courseRepository.save(curso);
    }

    @Transactional
    public void deletarCurso(long cursoId, JwtAuthenticationToken authentication) {

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

    private void validarCategoriaId(long categoriaId) {
        if (categoriaId < 1 || categoriaId > 5) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
    }

}