package com.unithub.controller;

import com.unithub.dto.CriarCursoDTO;
import com.unithub.model.Categorys;
import com.unithub.model.Course;
import com.unithub.service.CourseService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Course> criarCurso(@RequestBody CriarCursoDTO dto) {
        Course novoCurso = courseService.criarCurso(dto.nome(), dto.categoriaId());
        return ResponseEntity.ok(novoCurso);
    }

    @DeleteMapping("/{cursoId}")
    public ResponseEntity<Void> deletarCurso(@PathVariable Long cursoId) {
        courseService.deletarCurso(cursoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Course>> listarCursos() {
        List<Course> cursos = courseService.listarCursos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/categorias/alunos")
    public ResponseEntity<List<Categorys>> listarCategoriasAlunos() {
        List<Categorys> categorias = courseService.listarCategoriasAlunos();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/categorias/eventos")
    public ResponseEntity<List<Categorys>> listarCategoriasEventos() {
        List<Categorys> categorias = courseService.listarCategoriasEventos();
        return ResponseEntity.ok(categorias);
    }
}