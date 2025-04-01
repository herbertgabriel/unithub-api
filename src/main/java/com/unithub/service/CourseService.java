package com.unithub.service;

import com.unithub.model.Categorys;
import com.unithub.model.Course;
import com.unithub.model.Role;
import com.unithub.repository.CourseRepository;
import com.unithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // Adicionar Admin
    @Transactional
    public Course criarCurso(String nome, long categoriaId, JwtAuthenticationToken authentication) {

        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new RuntimeException("You don't have permission");
        }

        Categorys categoria = Categorys.fromId(categoriaId);

        if (categoria == Categorys.OFICIAL || categoria == Categorys.NAO_OFICIAL) {
            throw new IllegalArgumentException("Categorias 'Evento Oficial' e 'Evento Não Oficial' não são permitidas para cursos.");
        }

        Course curso = new Course();
        curso.setNome(nome);
        curso.setCategoria(categoria);

        return courseRepository.save(curso);
    }

    @Transactional
    public void deletarCurso(Long cursoId, JwtAuthenticationToken authentication) {

        var usuario = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        var isOrganizadorOrAdmin = usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ORGANIZADOR.name()) ||
                        role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(!isOrganizadorOrAdmin){
            throw new RuntimeException("You don't have permission");
        }

        var curso = courseRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso not found"));
        courseRepository.delete(curso);
    }

    public List<Course> listarCursos() {
        return courseRepository.findAll();
    }

    public List<Categorys> listarCategoriasAlunos() {
        return List.of(Categorys.values()).stream()
                .filter(categoria -> categoria != Categorys.OFICIAL && categoria != Categorys.NAO_OFICIAL)
                .toList();
    }

    public List<Categorys> listarCategoriasEventos() {
        return List.of(Categorys.values());
    }
}