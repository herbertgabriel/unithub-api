package com.unithub.controller;

import com.unithub.dto.CriarCursoDTO;
import com.unithub.model.Category;
import com.unithub.model.Course;
import com.unithub.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course> criarCurso(@RequestBody CriarCursoDTO dto, JwtAuthenticationToken authentication) {
        Course novoCurso = courseService.criarCurso(dto.nome(), dto.categoriaId(), authentication);
        return ResponseEntity.ok(novoCurso);
    }

    @DeleteMapping("/{cursoId}")
    public ResponseEntity<Void> deletarCurso(@PathVariable Long cursoId, JwtAuthenticationToken authentication) {
        courseService.deletarCurso(cursoId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Course>> listarCursos() {
        List<Course> cursos = courseService.listarCursos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/categorias/alunos")
    public ResponseEntity<List<Category>> listarCategoriasAlunos() {
        List<Category> categorias = courseService.listarCategoriasAlunos();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/categorias/eventos")
    public ResponseEntity<List<Category>> listarCategoriasEventos() {
        List<Category> categorias = courseService.listarCategoriasEventos();
        return ResponseEntity.ok(categorias);
    }
}