package com.unithub.controller;

import com.unithub.dto.request.course.CriarCursoDTO;
import com.unithub.dto.request.course.EditarCursoDTO;
import com.unithub.model.Category;
import com.unithub.model.Course;
import com.unithub.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Void> criarCurso(@RequestBody CriarCursoDTO dto, JwtAuthenticationToken authentication) {
        courseService.criarCurso(dto.nome(), dto.categoriaId(), authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{cursoId}")
    public Course editarCurso(@PathVariable long cursoId,
                              @RequestBody EditarCursoDTO dto,
                              JwtAuthenticationToken authentication) {
        return courseService.editarCurso(dto, cursoId, authentication);
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

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listarCategorias() {
        List<Category> categorias = courseService.listarCategorias();
        return ResponseEntity.ok(categorias);
    }

}