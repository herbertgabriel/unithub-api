package com.unithub.service;

import com.unithub.dto.request.course.EditarCursoDTO;
import com.unithub.model.Category;
import com.unithub.model.Course;
import com.unithub.model.Role;
import com.unithub.model.User;
import com.unithub.repository.CourseRepository;
import com.unithub.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCriarCurso_Success() {
        // Arrange
        String nome = "Curso de Java";
        long categoriaId = 1L;

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleOrganizador = new Role();
        roleOrganizador.setName(Role.Values.ORGANIZADOR.name());
        usuario.setRoles(Set.of(roleOrganizador));

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course curso = courseService.criarCurso(nome, categoriaId, authentication);

        // Assert
        assertNotNull(curso);
        assertEquals(nome, curso.getNome());
        assertEquals(Category.fromId(categoriaId), curso.getCategoria());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testCriarCurso_AccessDenied() {
        // Arrange
        String nome = "Curso de Java";
        long categoriaId = 1L;

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleAluno = new Role();
        roleAluno.setName(Role.Values.ALUNO.name());
        usuario.setRoles(Set.of(roleAluno));

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> courseService.criarCurso(nome, categoriaId, authentication));
    }

    @Test
    void testEditarCurso_Success() {
        // Arrange
        long cursoId = 1L;
        EditarCursoDTO dto = new EditarCursoDTO("Novo Curso", 2L);

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleAdmin = new Role();
        roleAdmin.setName(Role.Values.ADMIN.name());
        usuario.setRoles(Set.of(roleAdmin));

        Course curso = new Course();
        curso.setNome("Curso Antigo");
        curso.setCategoria(Category.TECNOLOGIA);

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(courseRepository.findById(cursoId)).thenReturn(Optional.of(curso));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course cursoEditado = courseService.editarCurso(dto, cursoId, authentication);

        // Assert
        assertNotNull(cursoEditado);
        assertEquals("Novo Curso", cursoEditado.getNome());
        assertEquals(Category.fromId(2L), cursoEditado.getCategoria());
        verify(courseRepository, times(1)).findById(cursoId);
        verify(courseRepository, times(1)).save(curso);
    }

    @Test
    void testEditarCurso_NotFound() {
        // Arrange
        long cursoId = 1L;
        EditarCursoDTO dto = new EditarCursoDTO("Novo Curso", 2L);

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleAdmin = new Role();
        roleAdmin.setName(Role.Values.ADMIN.name());
        usuario.setRoles(Set.of(roleAdmin));

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(courseRepository.findById(cursoId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> courseService.editarCurso(dto, cursoId, authentication));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Course not found", exception.getReason());
    }

    @Test
    void testDeletarCurso_Success() {
        // Arrange
        long cursoId = 1L;

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleAdmin = new Role();
        roleAdmin.setName(Role.Values.ADMIN.name());
        usuario.setRoles(Set.of(roleAdmin));

        Course curso = new Course();
        curso.setNome("Curso de Java");

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(courseRepository.findById(cursoId)).thenReturn(Optional.of(curso));

        // Act
        courseService.deletarCurso(cursoId, authentication);

        // Assert
        verify(courseRepository, times(1)).findById(cursoId);
        verify(courseRepository, times(1)).delete(curso);
    }

    @Test
    void testDeletarCurso_NotFound() {
        // Arrange
        long cursoId = 1L;

        User usuario = new User();
        usuario.setUserId(UUID.randomUUID());
        Role roleAdmin = new Role();
        roleAdmin.setName(Role.Values.ADMIN.name());
        usuario.setRoles(Set.of(roleAdmin));

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);

        when(authService.getAuthenticatedUser(authentication)).thenReturn(usuario);
        when(courseRepository.findById(cursoId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> courseService.deletarCurso(cursoId, authentication));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Course not found", exception.getReason());
    }

    @Test
    void testListarCursos() {
        // Arrange
        List<Course> cursos = List.of(new Course(), new Course());
        when(courseRepository.findAll()).thenReturn(cursos);

        // Act
        List<Course> result = courseService.listarCursos();

        // Assert
        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }
}