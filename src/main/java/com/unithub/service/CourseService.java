package com.unithub.service;

import com.unithub.model.Course;
import com.unithub.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Adicionar Admin
    @Transactional
    public Course criarCurso(String nome, long categoriaId) {
        Course.Categorys categoria = Course.Categorys.fromId(categoriaId);

        if (categoria == Course.Categorys.OFICIAL || categoria == Course.Categorys.NAO_OFICIAL) {
            throw new IllegalArgumentException("Categorias 'Evento Oficial' e 'Evento Não Oficial' não são permitidas para cursos.");
        }

        Course curso = new Course();
        curso.setNome(nome);
        curso.setCategoria(categoria);

        return courseRepository.save(curso);
    }

    @Transactional
    public void deletarCurso(Long cursoId) {
        var curso = courseRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso not found"));
        courseRepository.delete(curso);
    }

    public List<Course> listarCursos() {
        return courseRepository.findAll();
    }

    public List<Course.Categorys> listarCategoriasAlunos() {
        return List.of(Course.Categorys.values()).stream()
                .filter(categoria -> categoria != Course.Categorys.OFICIAL && categoria != Course.Categorys.NAO_OFICIAL)
                .toList();
    }

    public List<Course.Categorys> listarCategoriasEventos() {
        return List.of(Course.Categorys.values());
    }
}